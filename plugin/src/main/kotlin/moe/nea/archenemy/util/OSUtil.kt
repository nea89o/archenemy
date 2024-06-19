package moe.nea.archenemy.util


object OSUtil {
	// TODO: replace this with a library
	enum class OsKind {
		WINDOWS,
		LINUX,
		OSX,
	}

	fun getOs(): OsKind {
		val osName = System.getProperty("os.name").lowercase()
		if (osName.contains("win")) return OsKind.WINDOWS
		if (osName.contains("nix") || osName.contains("nux")) return OsKind.LINUX
		if (osName.contains("mac")) return OsKind.OSX
		error("Unknown OS")
	}

	fun getOsClassifier(): String {
		return when (getOs()) {
			OsKind.WINDOWS -> "windows"
			OsKind.LINUX -> "linux"
			OsKind.OSX -> "osx"
		}
	}
}