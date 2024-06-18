package moe.nea.test

import net.minecraft.client.MinecraftClient

actual object TestClass {
	actual fun printTitle() {
		println(MinecraftClient.getInstance().currentScreen?.toString() ?: "no screen")
	}
}