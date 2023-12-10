import moe.nea.archenemy.MCSide

plugins {
    kotlin("multiplatform") version "1.9.10"
    id("moe.nea.archenemy.mojang")
}

repositories {
    mavenCentral()
}

kotlin {
    val allJvm by sourceSets.creating {
        this.dependencies {
        }
    }

    jvm("forge") {
        compilations.named("main").get().run {
            defaultSourceSet.dependsOn(allJvm)
            this.dependencies {
                val mappedMinecraft = mojang.mapJar(
                    mojang.minecraft("1.20.2", MCSide.CLIENT) as ModuleDependency,
                    mojang.officialMappings("1.20.2", MCSide.CLIENT),
                    "official",
                    "named"
                )
                implementation(mappedMinecraft)
            }
        }
    }
//    jvm("fabric") {
//        compilations.named("main").get().run {
//            defaultSourceSet.dependsOn(allJvm)
//        }
//    }
}

