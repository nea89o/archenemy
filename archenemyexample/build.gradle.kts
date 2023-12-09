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
                implementation(mojang.minecraft("1.8.9", MCSide.CLIENT))
            }
        }
    }
//    jvm("fabric") {
//        compilations.named("main").get().run {
//            defaultSourceSet.dependsOn(allJvm)
//        }
//    }
}

