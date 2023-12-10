package moe.nea.archenemy.mojang

import moe.nea.archenemy.util.DownloadUtils
import moe.nea.archenemy.util.getNullsafeIdentifier
import moe.nea.archenemy.util.updateField
import net.minecraftforge.artifactural.api.artifact.Artifact
import net.minecraftforge.artifactural.api.artifact.ArtifactIdentifier
import net.minecraftforge.artifactural.api.artifact.ArtifactType
import net.minecraftforge.artifactural.api.repository.Repository
import net.minecraftforge.artifactural.base.artifact.StreamableArtifact
import org.gradle.api.artifacts.ModuleDependency
import java.security.MessageDigest

class MappedRepositoryProvider(
    val sharedExtension: ArchenemyMojangExtension
) : Repository {

    data class MappedCoordinates(
        val dependency: ModuleDependency,
        val mappings: MappingDependency,
        val from: String,
        val to: String,
    ) {
        val transformerHash by lazy {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.updateField("name", dependency.name)
            messageDigest.updateField("from", from)
            messageDigest.updateField("to", to)
            messageDigest.updateField("version", dependency.version ?: "null")
            messageDigest.updateField("group", dependency.group ?: "null")
            mappings.updateHash(messageDigest)
            DownloadUtils.bytesToHex(messageDigest.digest())
        }
    }

    private val providers = mutableMapOf<String, MappedCoordinates>()
    private val cacheDir = sharedExtension.getLocalCacheDirectory().resolve("minecraft-transformer-cache")


    fun getDependencyCoordiante(coordinates: MappedCoordinates): String {
        providers[coordinates.transformerHash] = coordinates
                //-${coordinates.to}.${coordinates.dependency.group}
        return "archenemy.remapped.${coordinates.transformerHash}.${coordinates.mappings.title()}:${coordinates.dependency.name}:${coordinates.dependency.version}"
    }

    fun getDependencyCoordiante(
        dependency: ModuleDependency,
        mappings: MappingDependency,
        from: String,
        to: String
    ): String {
        val coordinates = MappedCoordinates(dependency, mappings, from, to)
        return getDependencyCoordiante(coordinates)
    }


    override fun getArtifact(identifier: ArtifactIdentifier?): Artifact {
        if (identifier == null) return Artifact.none()
        if (!identifier.group.startsWith("archenemy.remapped.")) return Artifact.none()
        if (identifier.extension != "jar") return Artifact.none() // TODO: support other artifacts (and poms)
        val hash = identifier.group.split(".")[2]
        val coordinates = providers[hash] ?: error("Unregistered mapped dependency $identifier")
        val (group, name, version) = getDependencyCoordiante(coordinates).split(":")
        if (group != identifier.group || name != identifier.name || version != identifier.version)
            error("Inconsistent mapped dependency $identifier (expected $coordinates)")

        return getArtifact(coordinates, getNullsafeIdentifier(identifier)) ?: Artifact.none()
    }

    private fun getArtifact(coordinates: MappedCoordinates, identifier: ArtifactIdentifier): Artifact? {
        if ((identifier.classifier ?: "") != "") return null
        return StreamableArtifact.ofStreamable(identifier, ArtifactType.BINARY) {
            val files = sharedExtension.project.configurations.detachedConfiguration(
                coordinates.dependency,
            ).also { it.isTransitive = false }.resolve()
            // TODO: move away from classifiers. those are *evil*.
            // for now i will just manually append -client
            // or figure out how loom does it, i suppose
            val sourceFile = files.singleOrNull { true } ?: error("Only support single file dependencies rn")
            coordinates.mappings.resolveMappingsOnce(sharedExtension)
            val targetFile = cacheDir.resolve(coordinates.transformerHash + ".jar")
            targetFile.parentFile.mkdirs()
            if (!targetFile.exists()) {
                coordinates.mappings.applyMapping(
                    sourceFile = sourceFile,
                    targetFile = targetFile,
                    sourceNameSpace = coordinates.from,
                    targetNameSpace = coordinates.to
                )
            }
            targetFile.inputStream()
        }
    }
}