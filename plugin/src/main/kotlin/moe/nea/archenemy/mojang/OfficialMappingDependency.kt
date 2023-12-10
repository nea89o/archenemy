package moe.nea.archenemy.mojang

import moe.nea.archenemy.MCSide
import moe.nea.archenemy.util.update
import moe.nea.archenemy.util.updateField
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Provider
import java.io.File
import java.security.MessageDigest

class OfficialMappingDependency(
    val side: MCSide,
    val version: String,
    val dependency: Provider<Dependency>
) : MappingDependency,
    Provider<Dependency> by dependency {

    override fun updateHash(digest: MessageDigest) {
        digest.update("official")
        digest.updateField("side", side.toString())
        digest.updateField("version", version)
    }

    override fun title(): String {
        return "official-$side-${version.replace(".","_")}"
    }

    override fun findMapping(files: Set<File>): File? {
        return files.singleOrNull {
            it.name == "minecraft-${version}-${side}-mappings.txt"
        }
    }

    override fun applyMapping(
        mappingsFile: File,
        sourceFile: File,
        targetFile: File,
        sourceNameSpace: String,
        targetNameSpace: String
    ) {
        sourceFile.copyTo(targetFile)
    }

}