package moe.nea.archenemy.util

import java.security.MessageDigest

fun MessageDigest.updateField(text: String, value: String) {
    this.update(text)
    this.update(":")
    this.update(value)
    this.update(";")
}

fun MessageDigest.update(text: String) {
    this.update(text.encodeToByteArray())
}