package moe.nea.archenemy.mojang

import org.gradle.api.Plugin
import org.gradle.api.Project

class ArchenemyMojangPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.rootProject.tasks.maybeCreate(
            "downloadMinecraftVersionManifest",
            DownloadMinecraftVersionManifest::class.java
        )

        val rootExt = project.rootProject.extensions
        if (rootExt.findByName("archenemyShared") == null) {
            rootExt.create("archenemyShared", ArchenemySharedExtension::class.java, project.rootProject)
        }
        val mojang = project.extensions.create(
            "mojang", ArchenemyMojangExtension::class.java, project,
        )
    }
}