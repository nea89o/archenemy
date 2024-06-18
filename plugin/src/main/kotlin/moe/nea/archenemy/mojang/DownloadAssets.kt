package moe.nea.archenemy.mojang

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import moe.nea.archenemy.util.DownloadUtils
import moe.nea.archenemy.util.sharedExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.URL
import javax.inject.Inject

abstract class DownloadAssets @Inject constructor(
    @get:Input
    val version: String
) : DefaultTask() {


    init {
        dependsOn(project.sharedExtension.getDownloadVersionMetadataTask(version))
    }

    @Serializable
    data class AssetIndexList(
        val objects: Map<String, AssetIndexFile>
    )

    @Serializable
    data class AssetIndexFile(
        val hash: String,
        val size: Long,
    )

    @Internal
    fun getAssetDir() =
        project.sharedExtension
            .getGlobalCacheDirectory()
            .resolve("assets")

    @Internal
    fun getAssetIndex() =
        project.sharedExtension
            .getDownloadVersionMetadataTask(version)
            .getVersionMetadataNow()
            .assetIndex

    @TaskAction
    fun execute() {
        val manifest =
            project.sharedExtension
                .getDownloadVersionMetadataTask(version)
                .getVersionMetadata()
        val indexFile = getAssetDir()
            .resolve("indexes")
            .resolve(manifest.assetIndex.id + ".json")
        DownloadUtils.downloadFile(URL(manifest.assetIndex.url), manifest.assetIndex.sha1, indexFile)
        val assetIndexList: AssetIndexList = indexFile.inputStream().use(Json::decodeFromStream)
        for ((path, entry) in assetIndexList.objects) {
            downloadAsset(entry)
        }
    }

    private fun downloadAsset(entry: AssetIndexFile) {
        val prefix = entry.hash.substring(0, 2)
        val file = getAssetDir().resolve("objects").resolve(prefix).resolve(entry.hash)
        DownloadUtils.downloadFile(
            URL("https://resources.download.minecraft.net/$prefix/${entry.hash}"),
            entry.hash,
            file
        )
    }
}