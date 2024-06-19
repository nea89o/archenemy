package moe.nea.aee.forge

import net.minecraft.launchwrapper.ITweaker
import net.minecraft.launchwrapper.LaunchClassLoader
import java.io.File

class Tweaker : ITweaker {
	val arguments = mutableListOf<String>()

	override fun acceptOptions(
		args: List<String>, gameDir: File?,
		assetsDir: File?, profile: String?
	) {
		arguments.addAll(args)
		if (gameDir != null){
			arguments.add("--gameDir")
			arguments.add(gameDir.absolutePath)
		}
		if (assetsDir != null){
			arguments.add("--assetsDir")
			arguments.add(assetsDir.absolutePath)
		}
		if (profile != null){
			arguments.add("--version")
			arguments.add(profile)
		}
	}

	override fun injectIntoClassLoader(classLoader: LaunchClassLoader) {
	}

	override fun getLaunchTarget(): String {
		return "net.minecraft.client.main.Main"
	}

	override fun getLaunchArguments(): Array<String> {
		return arguments.toTypedArray()
	}
}
