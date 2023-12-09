package moe.nea.archenemy.mojang

import org.gradle.api.Project
import java.io.File

abstract class ArchenemySharedExtension(val rootProject: Project) {
    init {
        require(rootProject == rootProject.rootProject)
    }

    fun getLocalCacheDirectory(): File {
        return rootProject.rootDir.resolve(".gradle/archenemy")
    }

    fun getDownloadMinecraftVersionManifestTask(): DownloadMinecraftVersionManifest {
        return rootProject.tasks.getByName("downloadMinecraftVersionManifest") as DownloadMinecraftVersionManifest
    }

    val minecraftProvider = MinecraftProvider(this)
}