import moe.nea.archenemy.MCSide

plugins {
    kotlin("multiplatform") version "1.9.10"
    id("moe.nea.archenemy.mojang")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
}
val minecraft = mojang.minecraft("1.20.2", MCSide.CLIENT) as ModuleDependency
val officialMappings = mojang.officialMappings(
    "1.20.2", MCSide.CLIENT
)
val yarnMappings = mojang.yarnMappings(dependencies.create("net.fabricmc:yarn:1.20.2+build.4:v2"))
val intermediaryMappings = mojang.yarnMappings(dependencies.create("net.fabricmc:intermediary:1.20.2:v2"))

val whateverAttribute = Attribute.of("whatever", String::class.java)
kotlin {
    val allJvm by sourceSets.creating {
        this.dependencies {
        }
    }

    jvm("forge") {
        attributes.attribute(whateverAttribute, "forge")
        compilations.named("main").get().run {
            defaultSourceSet.dependsOn(allJvm)
            this.dependencies {
                implementation(
                    mojang.mapJar(
                        minecraft,
                        officialMappings,
                        "official",
                        "named"
                    )
                )
            }
        }
    }
    jvm("fabric") {
        attributes.attribute(whateverAttribute, "fabric")
        compilations.named("main").get().run {
            defaultSourceSet.dependsOn(allJvm)
            this.dependencies {
                val thingy = mojang.mapJar(
                    minecraft,
                    intermediaryMappings,
                    "official",
                    "intermediary"
                )
                implementation(
                    mojang.mapJar(
                        thingy as ModuleDependency,
                        yarnMappings,
                        "intermediary",
                        "named"
                    )
                )
            }
        }
    }
}

