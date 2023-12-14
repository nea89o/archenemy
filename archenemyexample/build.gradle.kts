import moe.nea.archenemy.MCSide

plugins {
    kotlin("multiplatform") version "1.9.10"
    id("moe.nea.archenemy.mojang")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
}
val minecraftClient = mojang.minecraft("1.20.2", MCSide.CLIENT) as ModuleDependency
val minecraftServer = mojang.minecraft("1.20.2", MCSide.CLIENT) as ModuleDependency
val officialMappings = mojang.officialMappings(
    "1.20.2", MCSide.CLIENT
)
val yarnMappings = mojang.yarnMappings(dependencies.create("net.fabricmc:yarn:1.20.2+build.4:v2"))
val intermediaryMappings = mojang.intermediaryMappings("1.20.2")

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
                        minecraftClient,
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
                val intermediaryClient = mojang.mapJar(
                    minecraftClient,
                    intermediaryMappings,
                    "official",
                    "intermediary"
                )
                val intermediaryServer = mojang.mapJar(
                    minecraftServer,
                    intermediaryMappings,
                    "official",
                    "intermediary"
                )
                val thingy = mojang.mergeJar(
                    intermediaryClient, intermediaryServer
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

