import net.minecraft.client.MinecraftClient
import net.minecraft.server.dedicated.DedicatedServer

actual fun doStuff(args: Int) {
    val client = MinecraftClient.getInstance()
    val dedicated: DedicatedServer = TODO()
}