package diana

import diana.commands.Command
import diana.core.Warp
import diana.handlers.*
import diana.soopy.WebsiteConnection
import kotlinx.coroutines.CoroutineScope
import net.minecraft.client.Minecraft
import net.minecraft.util.Vec3
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.awt.Color
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext

@Mod(
    name = Diana.modName,
    modid = Diana.modID,
    version = Diana.version,
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.8.9]",
    modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter"
)
class Diana {
    companion object {
        const val modName = "Diana"
        const val modID = "diana"
        const val version = "0.5-beta.2"
        const val chatTitle = "§3[Diana]§r "
        val mc = Minecraft.getMinecraft()
        val config = diana.config.Config
        val scope = CoroutineScope(EmptyCoroutineContext)
        val warps = arrayOf(
            Warp(Vec3(-3.0, 69.0, -70.0), "hub") { true },
            Warp(Vec3(-250.0, 129.0, -45.0), "castle") { config.castleWarp },
            Warp(Vec3(-162.0, 61.0, -99.0), "crypt") { config.cryptWarp },
            Warp(Vec3(91.0, 74.0, 173.0), "da") { config.daWarp },
            Warp(Vec3(-76.0, 75.0, 80.0), "museum") { config.museumWarp },
            Warp(Vec3(42.0, 121.0, 69.0), "wizard") { config.wizardWarp }
        )
        var waypointStyles: Map<Int, Pair<String, Color>> = mapOf(
            1 to ("§aStart" to config.startColor),
            2 to ("§cMob" to config.mobColor),
            3 to ("§fMob/Treasure" to config.unknownColor),
            4 to ("§eTreasure" to config.treasureColor)
        )
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        File(event.modConfigurationDirectory, "").mkdirs()
        ClientCommandHandler.instance.registerCommand(Command())
        listOf(
            this,
            EntityHandler,
            Keybindings,
            LocationHandler,
            PacketHandler,
            Render
        ).forEach(MinecraftForge.EVENT_BUS::register)
    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        Keybindings.keybindings.forEach { ClientRegistry.registerKeyBinding(it) }
        WebsiteConnection.onInit()
    }
}