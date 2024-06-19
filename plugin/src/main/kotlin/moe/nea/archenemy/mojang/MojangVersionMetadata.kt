package moe.nea.archenemy.mojang

import kotlinx.serialization.Serializable
import moe.nea.archenemy.util.OSUtil

@Serializable
data class MojangVersionMetadata(
	val assetIndex: AssetIndex,
	val downloads: Map<String, Download>,
	val libraries: List<Library>
) {

	fun getFilteredLibraries(): List<Library> {
		return libraries.filter { !it.name.contains("twitch-platform") && !it.name.contains("twitch-external-platform") }
	}

	@Serializable
	data class Library(
		val name: String,
		val natives: Map<String, String>? = null,
		val extract: ExtractOptions? = null,
	) : java.io.Serializable {
		fun getArtifactCoordinate(): String {
			val classifier = natives?.get(OSUtil.getOsClassifier())
			return name + (if (classifier != null) ":$classifier" else "")
		}

		@Serializable
		data class ExtractOptions(val exclude: List<String> = listOf()) : java.io.Serializable
	}

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