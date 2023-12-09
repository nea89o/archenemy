package moe.nea.archenemy.mojang

import java.io.File
import java.security.MessageDigest

class IdentityMinecraftTransformer : MinecraftTransformer {
    override fun updateHash(hash: MessageDigest) {
        hash.update("Identity")
    }

    override fun transformJar(oldJar: File, newJar: File) {
        oldJar.copyTo(newJar)
    }

}