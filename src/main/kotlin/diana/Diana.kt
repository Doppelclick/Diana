package diana

import diana.commands.Command
import diana.config.ConfigSystem
import diana.config.categories.CategoryRender
import diana.config.categories.CategoryWarps
import diana.core.Warp
import diana.handlers.*
import diana.soopy.WebsiteConnection
import diana.utils.Updater
import kotlinx.coroutines.CoroutineScope
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.awt.Color
import kotlin.coroutines.EmptyCoroutineContext

@Mod(
    name = Diana.modName,
    modid = Diana.modID,
    version = Diana.version,
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.8.9]"
)
class Diana {
    companion object {
        const val modName = "Diana"
        const val modID = "diana"
        const val version = "0.6-beta.1"
        const val chatTitle = "§3[Diana]§r "
        val mc = Minecraft.getMinecraft()
        val icon = ResourceLocation("diana", "griffin.png")
        val configSystem = ConfigSystem()
        val updater = Updater()
        val scope = CoroutineScope(EmptyCoroutineContext)
        val warps = arrayOf(
            Warp(Vec3(-3.0, 69.0, -70.0), "hub") { true },
            Warp(Vec3(-250.0, 129.0, -45.0), "castle") { CategoryWarps.castle },
            Warp(Vec3(-162.0, 61.0, -99.0), "crypt") { CategoryWarps.crypt },
            Warp(Vec3(91.0, 74.0, 173.0), "da") { CategoryWarps.da },
            Warp(Vec3(-76.0, 75.0, 80.0), "museum") { CategoryWarps.museum },
            Warp(Vec3(42.0, 121.0, 69.0), "wizard") { CategoryWarps.wizard }
        )
        var waypointStyles: Map<Int, Pair<String, Color>> = mapOf(
            1 to ("§aStart" to CategoryRender.startColor),
            2 to ("§cMob" to CategoryRender.mobColor),
            3 to ("§fMob/Treasure" to CategoryRender.unknownColor),
            4 to ("§eTreasure" to CategoryRender.treasureColor)
        )

        fun onShutDown() {
            configSystem.storeAll()
        }
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        configSystem.load()
        WebsiteConnection.preInit()
        ClientCommandHandler.instance.registerCommand(Command())
        listOf(
            this,
            BurrowSelector,
            EntityHandler,
            LocationHandler,
            Render
        ).forEach(MinecraftForge.EVENT_BUS::register)
    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        WebsiteConnection.onInit()
    }
}