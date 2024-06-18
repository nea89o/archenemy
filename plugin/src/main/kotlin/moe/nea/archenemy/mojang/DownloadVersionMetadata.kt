package moe.nea.archenemy.mojang

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import moe.nea.archenemy.util.DownloadUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URL
import javax.inject.Inject

abstract class DownloadVersionMetadata @Inject constructor(
    @get:Input
    val version: String,
) : DefaultTask() {
    @get:OutputFile
    abstract val file: RegularFileProperty


    init {
        file.convention(project.layout.buildDirectory.file("version-metadata/$version")).finalizeValue()
        dependsOn(
            project.extensions.getByType(ArchenemySharedExtension::class.java)
                .getDownloadMinecraftVersionManifestTask()
        )
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }

    @TaskAction
    fun downloadVersion() {
        val manifest =
            project.extensions.getByType(ArchenemySharedExtension::class.java).getDownloadMinecraftVersionManifestTask()
                .getManifestNow()
        val versionManifestUrl = manifest.versions.find { it.id == version }!!.url
        val parts = versionManifestUrl.split("/")
        val hash = if (parts.size == 7 && parts[5].length == 40) {
            parts[5]
        } else {
            error("uhhh hash not found")
        }
        DownloadUtils.downloadFile(
            URL(versionManifestUrl),
            hash,
            file.get().asFile
        )
    }

    @Internal
    fun getVersionMetadata(): MojangVersionMetadata {
        return file.get().asFile.inputStream().use(json::decodeFromStream)
    }

    @Internal
    fun getVersionMetadataNow(): MojangVersionMetadata {
        project.objects.fileCollection().from(file).files
        return getVersionMetadata()
    }
}