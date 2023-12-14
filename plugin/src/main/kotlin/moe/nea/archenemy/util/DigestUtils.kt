package moe.nea.archenemy.util

import org.gradle.api.artifacts.ModuleDependency
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

fun MessageDigest.updateGMV(name: String, moduleDependency: ModuleDependency) {
    this.updateField(
        name,
        (moduleDependency.group ?: "") + ":" + moduleDependency.name + ":" + (moduleDependency.version ?: "")
    )
}