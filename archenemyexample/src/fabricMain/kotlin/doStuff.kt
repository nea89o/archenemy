import net.minecraft.client.MinecraftClient

actual fun doStuff(args: Int) {
    val client = MinecraftClient.getInstance()
}