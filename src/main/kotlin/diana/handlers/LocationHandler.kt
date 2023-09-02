package diana.handlers

import diana.Diana.Companion.mc
import diana.config.Config
import diana.core.Burrows
import diana.core.Warp
import diana.core.Waypoint
import diana.utils.Updater
import diana.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.util.StringUtils
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import java.util.*

object LocationHandler {
    val locationPattern = Regex("⏣ (?<location>\\S+((\\s\\S+)?))")

    var ticks = 0

    private var onHypixel = false
    var inHub = false
        get() = if (Config.forceHub) true else field
    var doingDiana = false


    @SubscribeEvent
    fun serverConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        mc.currentServerData?.run {
            if (this.serverIP.lowercase(Locale.getDefault()).contains("hypixel.")) {
                onHypixel = true
                Updater().setUp()
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START ||! Config.toggle || mc.thePlayer == null || (ticks > 600 &&! inHub)) return
        if (!inHub) {
            ticks++
            if (ticks % 10 == 0) {
                inHub = checkHub()
                if (inHub) println("Joined Skyblock hub")
            }
        }

        if (!inHub || !doingDiana) return
        ticks++
        if (ticks % 20 == 0) {
            if (Config.guess) {
                if (Burrows.echo) {
                    if (System.currentTimeMillis() > Burrows.clicked + 4500) {
                        Burrows.echo = false
                    }
                }
            }
        }

        if (ticks % 200 == 0) {
            ticks = 0
            Burrows.waypoints.removeIf { it is Waypoint.InquisWaypoint && System.currentTimeMillis() > it.time + 60000 }
        }
    }

    //from Danker's Skyblock Mod under GNU 3.0 license
    fun checkHub(): Boolean {
        Minecraft.getMinecraft().thePlayer?.sendQueue?.playerInfoMap?.forEach {
            if (it != null && it.displayName != null) {
                val text = it.displayName.unformattedText
                if (text.startsWith("Area: ")) {
                    return text == "Area: Hub"
                }
            }
        }
        return false
    }

    fun getLocation(): String {
        Utils.sidebarLines.map { StringUtils.stripControlCodes(it).replace("⚽", "") }.filter { it.contains("⏣") }.forEach { line ->
            locationPattern.find(line)?.let { it.groups["location"]?.value?.let { location ->
                return if (location.contains("Museum")) "Museum" else location //to remove the "AAA's" Museum
            } }
        }
        return "Unknown"
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        inHub = false
        ticks = 0
        doingDiana = false

        Burrows.resetBurrow()
        Burrows.clicked = 0L
        Burrows.echo = false
        Burrows.dugBurrows.clear()
        Burrows.oldParticles.clear()
        Burrows.arrow = false
        Burrows.arrowStart = null
        Burrows.arrowDir = null
        Burrows.selected = null
        Burrows.burrow = null
        Burrows.waypoints.clear()
        Burrows.foundBurrows.clear()
        Warp.lastwarp = "undefined"

        Render.resetRender()
    }

    @SubscribeEvent
    fun onServerDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        onHypixel = false
    }
}