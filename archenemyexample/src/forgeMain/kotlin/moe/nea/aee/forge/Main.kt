package moe.nea.aee.forge

import net.minecraft.launchwrapper.ITweaker
import net.minecraft.launchwrapper.LaunchClassLoader
import java.io.File

class Tweaker : ITweaker {
	override fun acceptOptions(args: MutableList<String>?, gameDir: File?, assetsDir: File?, profile: String?) {
	}

	override fun injectIntoClassLoader(classLoader: LaunchClassLoader) {
	}

	override fun getLaunchTarget(): String {
		return "net.minecraft.client.main.Main"
	}

	override fun getLaunchArguments(): Array<String> {
		return arrayOf()
	}
}
