package moe.nea.archenemy.mojang

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URL

abstract class DownloadMinecraftVersionManifest : DefaultTask() {

    @get:OutputFile
    abstract val manifestFile: RegularFileProperty

    @get:Input
    abstract val manifestUrl: Property<String>

    init {
        manifestUrl.convention("https://launchermeta.mojang.com/mc/game/version_manifest.json")
        manifestFile.convention(project.layout.buildDirectory.file("mojang-version-manifest.json"))
    }

    @TaskAction
    fun downloadManifest() {
        val url = URL(manifestUrl.get())
        val file = manifestFile.asFile.get()
        file.parentFile.mkdirs()
        url.openStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    @Internal
    fun getManifestNow(): MojangVersionManifest {
        // Force resolution
        project.objects.fileCollection().from(manifestFile).files
        return getManifest().get()
    }

    @Internal
    fun getManifest(): Provider<MojangVersionManifest> {
        return manifestFile.asFile.map {
            val manifestText = it.readText()
            Json.decodeFromString<MojangVersionManifest>(manifestText)
        }
    }
}