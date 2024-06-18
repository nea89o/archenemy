includeBuild("..")
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.neoforged.net/releases")
	    maven("https://repo.nea.moe/releases")
	    mavenLocal()
    }
}