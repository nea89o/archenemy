package moe.nea.archenemy.mojang

import java.security.MessageDigest

interface CHashable {
    fun updateHash(digest: MessageDigest)
}
