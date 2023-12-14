package moe.nea.archenemy.mojang

import moe.nea.archenemy.util.DownloadUtils
import moe.nea.archenemy.util.getNullsafeIdentifier
import net.minecraftforge.artifactural.api.artifact.Artifact
import net.minecraftforge.artifactural.api.artifact.ArtifactIdentifier
import net.minecraftforge.artifactural.api.artifact.Streamable
import net.minecraftforge.artifactural.api.repository.Repository
import org.gradle.api.artifacts.Dependency
import java.io.File
import java.security.MessageDigest

abstract class AbstractTransformerRepository<T : CHashable>(
    val repoIdentifier: String,
    val extension: ArchenemyMojangExtension,
) : Repository {
    init {
        require(!repoIdentifier.contains("."))
    }

    data class HashContainer<T : CHashable>(val t: T) {
        val transformerHash = kotlin.run {
            val digest = MessageDigest.getInstance("SHA-256")
            t.updateHash(digest)
            DownloadUtils.bytesToHex(digest.digest())
        }
    }

    fun resolveDirect(vararg dependency: Dependency): Set<File> {
        return extension.project.configurations.detachedConfiguration(*dependency).also { it.isTransitive = false }
            .resolve()
    }

    fun lazyTransform(file: File, function: (file: File) -> Unit): Streamable {
        return Streamable {
            if (!file.exists()) function(file)
            file.inputStream()
        }
    }

    private val providers: MutableMap<String, HashContainer<T>> = mutableMapOf()
    val baseDirectory = extension.getLocalCacheDirectory().resolve("precache-$repoIdentifier")
    fun getCoordinate(t: T): String {
        val container = HashContainer(t)
        providers[container.transformerHash] = container
        val c = getClassifier(t)
        return "archenemy.$repoIdentifier.${container.transformerHash}:${getName(t)}:${getVersion(t)}" + if (c != null) ":$c" else ""
    }

    protected abstract fun getName(t: T): String
    protected abstract fun getVersion(t: T): String
    protected open fun getClassifier(t: T): String? = null
    override fun getArtifact(identifier: ArtifactIdentifier?): Artifact {
        if (identifier == null) return Artifact.none()
        if (!identifier.group.startsWith("archenemy.${this.repoIdentifier}.")) return Artifact.none()
        val hash = identifier.group.split(".")[2]
        val provider = providers[hash] ?: error("Unregistered archenemy ${this.repoIdentifier} dependeny")
        val coordinate = getCoordinate(provider.t)
        require(coordinate.startsWith(identifier.group + ":" + identifier.name + ":" + identifier.version))
        if (identifier.extension == "pom") return Artifact.none()
        return getArtifact(
            getNullsafeIdentifier(identifier),
            provider.t,
            baseDirectory.resolve("$hash-${identifier.classifier}.${identifier.extension}")
        ) ?: Artifact.none()
    }

    protected abstract fun getArtifact(identifier: ArtifactIdentifier, value: T, file: File): Artifact?
}