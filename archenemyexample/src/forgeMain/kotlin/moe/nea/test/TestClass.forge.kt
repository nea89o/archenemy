package moe.nea.test

import net.minecraft.client.Minecraft

actual object TestClass {
	actual fun printTitle() {
		println(Minecraft.getMinecraft().currentScreen?.toString() ?: "no screen")
	}
}