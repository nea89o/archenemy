@file:UseSerializers(InstantSerializer::class)

package moe.nea.archenemy.mojang

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import moe.nea.archenemy.util.InstantSerializer
import java.time.Instant

@Serializable
data class MojangVersionManifest(
    val latest: Promotions,
    val versions: List<VersionReference>
) {
    @Serializable
    data class VersionReference(
        val id: String,
        val type: String,
        val url: String,
        val time: Instant,
        val releaseTime: Instant,
    )

    @Serializable
    data class Promotions(
        val release: String,
        val snapshot: String,
    )
}