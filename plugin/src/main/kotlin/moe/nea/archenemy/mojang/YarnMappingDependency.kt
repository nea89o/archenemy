package moe.nea.archenemy.mojang

import moe.nea.archenemy.util.update
import moe.nea.archenemy.util.updateField
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.provider.Provider
import java.io.BufferedReader
import java.io.File
import java.io.StringReader
import java.security.MessageDigest
import java.util.zip.ZipFile

class YarnMappingDependency(
    val extension: ArchenemyMojangExtension,
    val dependency: ModuleDependency
) : MappingDependency,
    Provider<Dependency> by extension.project.provider({ dependency }) {
    var tinySource: String? = null

    init {
        val artifact = dependency.artifacts.single()
        require(artifact.classifier == "v2")
        require(artifact.extension == "jar")
    }

    override fun updateHash(digest: MessageDigest) {
        digest.update("yarn")
        digest.updateField("group", dependency.group ?: "null")
        digest.updateField("name", dependency.name)
        digest.updateField("version", dependency.version ?: "null")
        digest.updateField("classifier", dependency.artifacts.single().classifier ?: "")
        digest.updateField("extension", dependency.artifacts.single().extension ?: "")
    }

    override fun title(): String {
        return "yarn-${dependency.group?.replace(".", "-")}-${dependency.name}-${dependency.version?.replace(".", "-")}"
    }

    override fun resolveMappingsOnce(extension: ArchenemyMojangExtension) {
        if (tinySource != null) return
        val v2Jar = extension.project.configurations.detachedConfiguration(get()).resolve().single()
        ZipFile(v2Jar).use { zip ->
            val ze = zip.getEntry("mappings/mappings.tiny")
            zip.getInputStream(ze).use { inp ->
                tinySource = inp.reader().readText()
            }
        }
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
                    BufferedReader(StringReader(tinySource ?: error("Mappings have never been resolved"))),
                    sourceNameSpace,
                    targetNameSpace,
                )
            )
            .build()
	    // TODO: this does not copy over the non class file resources, seemingly
        OutputConsumerPath.Builder(targetFile.toPath()).build().use { output ->
            remapper.readInputs(sourceFile.toPath())
            remapper.apply(output)
        }
        remapper.finish()
    }
}