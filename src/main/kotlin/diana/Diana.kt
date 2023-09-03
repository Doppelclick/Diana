package diana

import diana.commands.Command
import diana.config.Config
import diana.core.Warp
import diana.handlers.*
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
        const val version = "0.4.3"
        const val chatTitle = "§3[Diana]§r "
        val mc = Minecraft.getMinecraft()
        val warps = arrayOf(
            Warp(Vec3(-3.0,69.0,-70.0), "hub") { true },
            Warp(Vec3(-250.0,129.0,-45.0), "castle") { Config.castleWarp },
            Warp(Vec3(-162.0,61.0,-99.0), "crypt") { Config.cryptWarp },
            Warp(Vec3(91.0,74.0,173.0), "da") { Config.daWarp },
            Warp(Vec3(-76.0,75.0,80.0), "museum") { Config.museumWarp },
            Warp(Vec3(42.0, 121.0, 69.0), "wizard") { Config.wizardWarp }
        )
        var waypointStyles: Map<Int, Pair<String, Color>> = mapOf(
            1 to ("§aStart" to Config.startColor),
            2 to ("§cMob" to Config.mobColor),
            3 to ("§fMob/Treasure" to Config.unknownColor),
            4 to ("§eTreasure" to Config.treasureColor)
        )
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        File(event.modConfigurationDirectory, "").mkdirs()
    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        ClientCommandHandler.instance.registerCommand(Command())
        listOf(
            this,
            EntityHandler,
            Keybindings,
            LocationHandler,
            PacketHandler,
            Render
        ).forEach(MinecraftForge.EVENT_BUS::register)
        Keybindings.keybindings.forEach { ClientRegistry.registerKeyBinding(it) }
    }
}