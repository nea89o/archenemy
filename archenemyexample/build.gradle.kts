import moe.nea.archenemy.MCSide

plugins {
	kotlin("multiplatform") version "1.9.22"
	id("moe.nea.archenemy.mojang")
}

repositories {
	mavenCentral()
	maven("https://maven.fabricmc.net")
	maven("https://repo.nea.moe/releases")
	mavenLocal()
}
kotlin.jvmToolchain(8)

val whateverAttribute = Attribute.of("whatever", String::class.java)
val allJvm by kotlin.sourceSets.creating {
	this.dependencies {
	}

}
val forge = kotlin.jvm("forge") {
	attributes.attribute(whateverAttribute, "forge")
	compilations.named("main").get().run {
		defaultSourceSet.dependsOn(allJvm)
		this.dependencies {
			val mcpMappings = mojang.yarnMappings(dependencies.create("moe.nea.mcp:mcp-yarn:1.8.9:v2"))
			val minecraftClient = mojang.minecraft("1.8.9", MCSide.CLIENT)
			val mappedClient = mojang.mapJar(minecraftClient, mcpMappings, "official", "named")
			implementation(mappedClient)
			implementation("net.minecraft:launchwrapper:1.12")
			mojang.libraries("1.8.9").forEach(::implementation)
		}
	}
}
val mainForge = forge.compilations.getByName("main")
val fabric = kotlin.jvm("fabric") {
	attributes.attribute(whateverAttribute, "fabric")
	compilations.named("main").get().run {
		defaultSourceSet.dependsOn(allJvm)
		this.dependencies {
			val minecraftClient = mojang.minecraft("1.20.2", MCSide.CLIENT)
			val minecraftServer = mojang.minecraft("1.20.2", MCSide.SERVER)
			val intermediaryMappings = mojang.intermediaryMappings("1.20.2")
			val yarnMappings = mojang.yarnMappings(dependencies.create("net.fabricmc:yarn:1.20.2+build.4:v2"))
			val intermediaryClient = mojang.mapJar(
				minecraftClient,
				intermediaryMappings,
				"official",
				"intermediary"
			)
			val intermediaryServer = mojang.mapJar(
				minecraftServer,
				intermediaryMappings,
				"official",
				"intermediary"
			)
			val thingy = mojang.mergeJar(
				intermediaryClient, intermediaryServer
			)
			implementation(
				mojang.mapJar(
					thingy,
					yarnMappings,
					"intermediary",
					"named"
				)
			)
		}
	}
}



tasks.create("runForge189", JavaExec::class) {
	val extractNatives = mojang.natives("1.8.9")
	dependsOn(extractNatives)
	description = "Run Forge 1.8.9"
	group = ApplicationPlugin.APPLICATION_GROUP
	mainClass.set("net.minecraft.launchwrapper.Launch")
	val downloadTask = archenemyShared.getDownloadAssetsTask("1.8.9")
	dependsOn(downloadTask)
	classpath(mainForge.runtimeDependencyFiles, tasks.getByName("forgeJar"))
	javaLauncher.set(javaToolchains.launcherFor {
		this.languageVersion.set(JavaLanguageVersion.of(8))
	})
	val runFolder = project.file("run")
	runFolder.mkdirs()
	workingDir(runFolder)
	jvmArguments.add("-Djava.library.path=${extractNatives.getNativeDirectoryPath().absolutePath}")
	doFirst {
		args(
			"--assetsDir",
			downloadTask.getAssetDir().absolutePath,
			"--assetIndex",
			downloadTask.getAssetIndex().id,
			"--accessToken",
			"undefined",
			"--gameDir",
			runFolder.absolutePath,
			"--tweakClass",
			"moe.nea.aee.forge.Tweaker",
			"--version",
			"1.8.9"
		)
	}
}

