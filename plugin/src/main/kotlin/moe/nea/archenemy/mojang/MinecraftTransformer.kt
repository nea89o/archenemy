package moe.nea.archenemy.mojang

import java.io.File
import java.security.MessageDigest

interface MinecraftTransformer {
    fun updateHash(hash: MessageDigest)
    fun transformJar(oldJar: File, newJar: File)
}
