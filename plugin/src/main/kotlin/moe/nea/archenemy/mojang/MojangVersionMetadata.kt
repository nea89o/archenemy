package moe.nea.archenemy.mojang

import kotlinx.serialization.Serializable

@Serializable
data class MojangVersionMetadata(
    val assetIndex: AssetIndex,
    val downloads: Map<String, Download>,
    val libraries: List<Library>
) {
    @Serializable
    data class Library(
        val name: String,
    )
    @Serializable
    data class Download(
        val sha1: String,
        val size: Long,
        val url: String,
    )

    @Serializable
    data class AssetIndex(
        val id: String,
        val sha1: String,
        val size: Long,
        val totalSize: Long,
        val url: String,
    )

}