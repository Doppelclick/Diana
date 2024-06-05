package diana.handlers

import com.google.common.collect.ComparisonChain
import diana.Diana.Companion.mc
import diana.config.categories.CategoryDebug
import diana.core.Burrows
import diana.core.Warp
import diana.soopy.SoopyV2Server
import diana.utils.Updater
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.util.StringUtils
import net.minecraft.world.WorldSettings
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import java.lang.reflect.Method
import java.util.*

object LocationHandler {
    private val serverPattern = Regex("\\d\\d/\\d\\d/\\d\\d (?<type>[mM])(?<server>\\S+\\D)")
    private val locationPattern = Regex("⏣ (?<location>\\S+((\\s\\S+)?))")

    private var onHypixel = false
    var area: String = "UNKNOWN"
    var location: String = "UNKNOWN" //more specific than area
    val inHub get() = if (CategoryDebug.forceHub) true else area == "Hub"
    var doingDiana = false

    var lastServer: String? = null
    var lastSentServer: Long = 0


    @SubscribeEvent
    fun serverConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        if (onHypixel) return
        mc.currentServerData?.run {
            if (this.serverIP.lowercase().contains("hypixel.")) {
                onHypixel = true
                Updater().check()
            }
        }
    }

    fun onScoreboard(packet: S3EPacketTeams) {
        if (packet.action != 2) return
        StringUtils.stripControlCodes(
            packet.players.joinToString(
                " ",
                prefix = packet.prefix,
                postfix = packet.suffix
            )
        )?.let { line ->
            serverPattern.find(line)?.let { match ->
                match.groups["server"]?.value?.let {
                    val server = (if (match.groups["type"]?.value == "M") "mega" else "mini") + it
                    if (lastServer != server && area != "UNKNOWN" && location != "UNKNOWN") {
                        lastSentServer = System.currentTimeMillis()
                        SoopyV2Server.setServer(server, area, location)
                    }
                    lastServer = server
                }
            } ?: locationPattern.find(line)?.let {
                val sendServer = location == "UNKNOWN"
                it.groups["location"]?.value?.let { loc ->
                    location = if (loc.contains("Museum")) "Museum" else loc
                }
                if (sendServer && area != "UNKNOWN" && lastServer != null) {
                    lastSentServer = System.currentTimeMillis()
                    SoopyV2Server.setServer(lastServer!!, area, location)
                }
            }
        }
    }

    object TabList {
        private val TabList = TreeSet(TabListComparator())
        private val playerInfoClass = NetworkPlayerInfo::class.java
        private val setGameTypeMethod: Method = playerInfoClass.getDeclaredMethod("func_178839_a", WorldSettings.GameType::class.java).apply { isAccessible = true }
        //private val setResponseTimeMethod: Method = playerInfoClass.getDeclaredMethod("func_178838_a", Int::class.java).apply { isAccessible = true }
        private val areaPattern = Regex("§r§\\S§lArea: §r§7(?<area>.+)§r")

        class TabListComparator : Comparator<NetworkPlayerInfo> {
            override fun compare(o1: NetworkPlayerInfo?, o2: NetworkPlayerInfo?): Int {
                if (o1?.gameProfile?.name == null) return -1
                else if (o2?.gameProfile?.name == null) return 0

                return ComparisonChain.start().compareTrueFirst(
                    o1.gameType != WorldSettings.GameType.SPECTATOR, o2.gameType != WorldSettings.GameType.SPECTATOR
                ).compare(
                    o1.safePlayerTeam()?.registeredName ?: "", o2.safePlayerTeam()?.registeredName ?: ""
                ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
            }
        }

        fun handlePacket(packet: S38PacketPlayerListItem) {
            val updated: ArrayList<NetworkPlayerInfo> = arrayListOf()
            packet.entries.forEach { player ->
                if (player == null) return@forEach
                if (packet.action == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
                    TabList.removeIf { it.gameProfile.id == player.profile.id }
                } else {
                    var networkPlayerInfo = TabList.find { it.gameProfile.id == player.profile.id }
                    if (packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                        networkPlayerInfo = NetworkPlayerInfo(player)
                        TabList.removeIf { it.gameProfile.id == player.profile.id }
                        TabList.add(networkPlayerInfo)
                    }
                    if (networkPlayerInfo != null) {
                        when (packet.action) {
                            S38PacketPlayerListItem.Action.ADD_PLAYER -> setGameTypeMethod.invoke(
                                networkPlayerInfo,
                                player.gameMode
                            )

                            S38PacketPlayerListItem.Action.UPDATE_GAME_MODE -> setGameTypeMethod.invoke(
                                networkPlayerInfo,
                                player.gameMode
                            )

                            S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME -> networkPlayerInfo.displayName =
                                player.displayName

                            else -> return@forEach
                        }
                        updated.add(networkPlayerInfo)
                    }
                }
            }
            updated.forEach {
                (it.displayName?.formattedText ?: it.gameProfile?.name)?.let { line ->
                    areaPattern.find(line)?.let { it.groups["area"]?.value }?.let { str ->
                        val sendServer = area == "UNKNOWN"
                        area = str
                        if (sendServer && location != "UNKNOWN" && lastServer != null) {
                            lastSentServer = System.currentTimeMillis()
                            SoopyV2Server.setServer(lastServer!!, area, location)
                        }
                    }
                }
            }
        }

        fun NetworkPlayerInfo.safePlayerTeam() = mc.theWorld?.scoreboard?.getPlayersTeam(this.gameProfile?.name)
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        location = "UNKNOWN"
        area = "UNKNOWN"
        doingDiana = false

        Burrows.resetBurrow()
        Burrows.clicked = 0L
        Burrows.echo = false
        Burrows.dugBurrows.clear()
        Burrows.oldParticles.clear()
        Burrows.arrow = false
        Burrows.arrowStart = null
        Burrows.arrowDir = null
        Burrows.burrow = null
        Burrows.waypoints.clear()
        Burrows.foundBurrows.clear()
        Warp.lastwarp = "undefined"

        BurrowSelector.selected = null

        Render.resetRender()
    }

    @SubscribeEvent
    fun onServerDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        onHypixel = false
    }
}