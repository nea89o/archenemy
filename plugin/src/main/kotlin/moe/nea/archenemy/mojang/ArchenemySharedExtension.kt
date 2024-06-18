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

    fun getDownloadVersionMetadataTask(version: String): DownloadVersionMetadata {
        val taskName = "downloadMinecraftVersionMetadata$version"
        val task = rootProject.tasks.findByName(taskName)
        if (task != null) {
            return task as DownloadVersionMetadata
        }
        return rootProject.tasks.create(taskName, DownloadVersionMetadata::class.java, version)
    }

    fun getDownloadAssetsTask(version: String): DownloadAssets {
        val taskName = "downloadMinecraftAssets$version"
        val task = rootProject.tasks.findByName(taskName)
        if (task != null) {
            return task as DownloadAssets
        }
        return rootProject.tasks.create(taskName, DownloadAssets::class.java, version)
    }

    fun getGlobalCacheDirectory(): File {
        return rootProject.gradle.gradleUserHomeDir.resolve("caches/archenemy")
    }

    val minecraftProvider = MinecraftProvider(this)
}