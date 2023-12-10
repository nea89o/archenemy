import net.minecraft.client.Minecraft

actual fun doStuff(args: Int) {
    val client = Minecraft.getInstance()
}