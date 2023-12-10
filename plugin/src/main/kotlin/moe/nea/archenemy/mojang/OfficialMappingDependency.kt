package moe.nea.archenemy.mojang

import moe.nea.archenemy.MCSide
import moe.nea.archenemy.util.update
import moe.nea.archenemy.util.updateField
import net.fabricmc.mappingio.format.ProGuardReader
import net.fabricmc.mappingio.format.Tiny2Writer
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Provider
import java.io.BufferedReader
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.security.MessageDigest

class OfficialMappingDependency(
    val side: MCSide,
    val version: String,
    val dependency: Provider<Dependency>
) : MappingDependency,
    Provider<Dependency> by dependency {
    var tinyv2Source: String? = null

    override fun updateHash(digest: MessageDigest) {
        digest.update("official")
        digest.updateField("side", side.toString())
        digest.updateField("version", version)
    }

    override fun title(): String {
        return "official-$side-${version.replace(".","_")}"
    }

    override fun resolveMappingsOnce(extension: ArchenemyMojangExtension) {
        if (tinyv2Source != null) return
        val buffer = StringWriter()
        val source = extension.project.configurations.detachedConfiguration(get()).resolve().single()
        source.reader().use {
            ProGuardReader.read(it, "named", "official", Tiny2Writer(buffer, false))
        }
        buffer.close()
        tinyv2Source = buffer.toString()
    }

    override fun applyMapping(
        sourceFile: File,
        targetFile: File,
        sourceNameSpace: String,
        targetNameSpace: String
    ) {
        val remapper = TinyRemapper.newRemapper()
            .withMappings(
                TinyUtils.createTinyMappingProvider(
                    BufferedReader(StringReader(tinyv2Source ?: error("Mappings have never been resolved"))),
                    sourceNameSpace,
                    targetNameSpace,
                )
            )
            .build()
        OutputConsumerPath.Builder(targetFile.toPath()).build().use { output ->
            remapper.readInputs(sourceFile.toPath())
            remapper.apply(output)
        }
        remapper.finish()

    }

}