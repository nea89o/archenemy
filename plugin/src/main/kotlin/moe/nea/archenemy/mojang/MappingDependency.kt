package moe.nea.archenemy.mojang

import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Provider
import java.io.File
import java.security.MessageDigest

interface MappingDependency : Provider<Dependency> {
    fun updateHash(digest: MessageDigest)
    fun title(): String
    fun findMapping(files: Set<File>): File?
    fun applyMapping(
        mappingsFile: File,
        sourceFile: File,
        targetFile: File,
        sourceNameSpace: String,
        targetNameSpace: String,
    )
}