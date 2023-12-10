package moe.nea.archenemy.mojang

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import moe.nea.archenemy.util.DownloadUtils
import moe.nea.archenemy.MCSide
import moe.nea.archenemy.util.getNullsafeIdentifier
import net.minecraftforge.artifactural.api.artifact.Artifact
import net.minecraftforge.artifactural.api.artifact.ArtifactIdentifier
import net.minecraftforge.artifactural.api.artifact.ArtifactType
import net.minecraftforge.artifactural.api.artifact.Streamable
import net.minecraftforge.artifactural.api.repository.Repository
import net.minecraftforge.artifactural.base.artifact.StreamableArtifact
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class MinecraftProvider(val sharedExtension: ArchenemySharedExtension) : Repository {

    data class MinecraftCoordinate(
        val version: String,
        val side: MCSide,
    )

    private val manifest by lazy {
        URL("https://launchermeta.mojang.com/mc/game/version_manifest.json").openStream().use {
            Json.decodeFromStream<MojangVersionManifest>(it)
        }
    }
    private val providers = mutableSetOf<MinecraftCoordinate>()
    private val versionManifest: MutableMap<String, MojangVersionMetadata> = ConcurrentHashMap()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun getDependencyCoordinate(minecraftCoordinate: MinecraftCoordinate): String {
        providers.add(minecraftCoordinate)
        return "archenemy.mojang:minecraft:${minecraftCoordinate.version}:${minecraftCoordinate.side}"
    }

    fun getMappingsDependencyCoordinate(minecraftCoordinate: MinecraftCoordinate): String {
        providers.add(minecraftCoordinate)
        return "archenemy.mojang:minecraft:${minecraftCoordinate.version}:${minecraftCoordinate.side}-mappings@txt"
    }


    private fun getVersionManifest(version: String): MojangVersionMetadata {
        return versionManifest.computeIfAbsent(version) {
            val versionMetadata = manifest.versions.find { it.id == version }
            if (versionMetadata == null)
                throw IOException("Invalid minecraft version $version")
            val metadata = URL(versionMetadata.url).openStream().use {
                json.decodeFromStream<MojangVersionMetadata>(it)
            }
            metadata
        }
    }

    private fun downloadMinecraft(coordinate: MinecraftCoordinate, mappings: Boolean): File {
        val metadata = getVersionManifest(coordinate.version)
        val downloadType = when (coordinate.side) {
            MCSide.CLIENT -> "client"
            MCSide.SERVER -> "server"
        } + if (mappings) "_mappings" else ""
        val download = metadata.downloads[downloadType]
            ?: throw IOException("Invalid minecraft side $downloadType for ${coordinate.version}")
        val targetFile =
            sharedExtension.getLocalCacheDirectory().resolve("minecraft-raw")
                .resolve("minecraft-${coordinate.version}-${coordinate.side}.${if (mappings) "txt" else "jar"}")
        DownloadUtils.downloadFile(URL(download.url), download.sha1, targetFile)
        return targetFile
    }

    private fun provideStreamableMinecraftJar(coordinate: MinecraftCoordinate): Streamable {
        return Streamable {
            downloadMinecraft(coordinate, false).inputStream()
        }
    }

    private fun provideStreamableMappings(coordinate: MinecraftCoordinate): Streamable {
        return Streamable {
            downloadMinecraft(coordinate, true).inputStream()
        }
    }


    override fun getArtifact(identifier: ArtifactIdentifier?): Artifact {
        if (identifier == null) return Artifact.none()
        if (identifier.name != "minecraft") return Artifact.none()
        if (identifier.group != "archenemy.mojang") return Artifact.none()
        if (identifier.extension == "pom") return Artifact.none()
        val coordinate =
            MinecraftCoordinate(identifier.version, MCSide.valueOf(identifier.classifier.removeSuffix("-mappings")))
        val isMappings = identifier.classifier.endsWith("-mappings")
        if (!providers.contains(coordinate))
            error("Unregistered minecraft dependency")
        if (identifier.extension == "jar" && !isMappings) {
            return StreamableArtifact.ofStreamable(
                getNullsafeIdentifier(identifier),
                ArtifactType.BINARY,
                provideStreamableMinecraftJar(coordinate)
            )
        }
        if (identifier.extension == "txt" && isMappings) {
            return StreamableArtifact.ofStreamable(
                getNullsafeIdentifier(identifier),
                ArtifactType.OTHER,
                provideStreamableMappings(coordinate)
            )
        }
        return Artifact.none()
    }
}

