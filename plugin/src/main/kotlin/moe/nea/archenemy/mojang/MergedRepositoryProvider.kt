package moe.nea.archenemy.mojang

import moe.nea.archenemy.util.readZipFs
import moe.nea.archenemy.util.update
import moe.nea.archenemy.util.updateGMV
import moe.nea.archenemy.util.zipFs
import net.minecraftforge.artifactural.api.artifact.Artifact
import net.minecraftforge.artifactural.api.artifact.ArtifactIdentifier
import net.minecraftforge.artifactural.api.artifact.ArtifactType
import net.minecraftforge.artifactural.base.artifact.StreamableArtifact
import org.gradle.api.artifacts.ModuleDependency
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

class MergedRepositoryProvider(archenemyMojangExtension: ArchenemyMojangExtension) :
    AbstractTransformerRepository<MergedRepositoryProvider.Coordinate>("merged", archenemyMojangExtension) {
    data class Coordinate(val base: ModuleDependency, val overlay: ModuleDependency) : CHashable {
        override fun updateHash(digest: MessageDigest) {
            digest.update("merged")
            digest.updateGMV("base", base)
            digest.updateGMV("overlay", overlay)
        }
    }

    override fun getName(t: Coordinate): String {
        return t.base.name
    }

    override fun getVersion(t: Coordinate): String {
        return t.base.version!!
    }

    override fun getArtifact(identifier: ArtifactIdentifier, value: Coordinate, file: File): Artifact? {
        if (!identifier.classifier.isNullOrBlank())
            return null
        if (identifier.extension != "jar") return null
        val baseJar = resolveDirect(value.base).singleOrNull() ?: return null
        val overlayJar = resolveDirect(value.overlay).singleOrNull() ?: return null
        return StreamableArtifact.ofStreamable(
            identifier,
            ArtifactType.BINARY,
            lazyTransform(file) { mergeJar(baseJar, overlayJar, it) })
    }

    private fun readClasses(
        path: Path,
        readClassFile: (String, ClassNode) -> Unit,
        readResource: (String, InputStream) -> Unit
    ) {
        path.readZipFs { path, inputStream ->
            if (path.startsWith("META-INF/")) return@readZipFs
            if (path.endsWith(".class")) {
                val classReader = ClassReader(inputStream)
                val classNode = ClassNode()
                classReader.accept(classNode, 0)
                readClassFile(path, classNode)
            } else {
                readResource(path, inputStream)
            }
        }
    }

    fun mergeClass(base: ClassNode, overlay: ClassNode): ClassNode {
        return ClassMerger().accept(base, overlay)
    }

    private fun mergeJar(baseJar: File, overlayJar: File, target: File) {
        target.toPath().zipFs().use { fs ->
            val classNodes = mutableMapOf<String, ClassNode>()
            fun readResource(path: String, inputStream: InputStream) {
                val p = fs.getPath(path)
                p.parent?.createDirectories()
                p.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            fun readClass(path: String, classNode: ClassNode) {
                classNodes.merge(path, classNode, ::mergeClass)
            }
            readClasses(baseJar.toPath(), ::readClass, ::readResource)
            readClasses(overlayJar.toPath(), ::readClass, ::readResource)
            classNodes.forEach { (path, node) ->
                val writer = ClassWriter(0)
                node.accept(writer)
                readResource(path, ByteArrayInputStream(writer.toByteArray()))
            }
        }
    }
}
