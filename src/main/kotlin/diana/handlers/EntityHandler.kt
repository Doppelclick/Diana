package diana.handlers

import diana.Diana.Companion.chatTitle
import diana.Diana.Companion.config
import diana.Diana.Companion.mc
import diana.core.Burrows
import diana.core.Warp
import diana.core.Waypoint
import diana.soopy.SoopyV2Server
import diana.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EntityHandler {
    @SubscribeEvent
    fun entity(event: EntityJoinWorldEvent) {
        if (!config.toggle || config.sendInq == 0 || !LocationHandler.doingDiana) return
        val entity = event.entity ?: return
        val player = Minecraft.getMinecraft().thePlayer ?: return
        if (entity.name.contains("Minos Inquisitor") && (config.sendInq == 2 || MessageHandler.inParty)) {
            if (Utils.maxDistance(player.positionVector, entity.positionVector) < 10 && Burrows.dugBurrows.any { it.distanceSq(entity.position) < 25 }) {
                val pos = BlockPos(entity.positionVector).down()
                if (config.inqWaypointMode != 0) {
                    SoopyV2Server.sendInquisData(pos, MessageHandler.partyMembers)
                }

                if (config.inqWaypointMode != 1) {
                    val warp = Warp.closest(entity.positionVector, false)?.name ?: "nothing"
                    player.sendChatMessage(("/" + (if (config.sendInq == 2) "a" else "p") + "c [Diana] Inquis! [" + pos.x + "," + pos.y + "," + pos.z + "] at ⏣ " + LocationHandler.location) + " warp: " + warp)
                }
            }
        }
    }

    @SubscribeEvent
    fun interact(event: PlayerInteractEvent) {
        if (!config.toggle || !LocationHandler.inHub) return
        val player = mc.thePlayer ?: return
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && config.guess) {
            if (System.currentTimeMillis() > Burrows.clicked + 3000) {
                if (player.heldItem?.getDisplayName()?.contains("Ancestral Spade") == true) {
                    Burrows.oldParticles = Burrows.particles
                    Burrows.resetBurrow()
                    Burrows.echo = true
                    LocationHandler.doingDiana = true
                    Burrows.clicked = System.currentTimeMillis()
                    Utils.startTimerTask(4501) {
                        if (config.guess && Burrows.echo) {
                            if (System.currentTimeMillis() > Burrows.clicked + 4500) {
                                Burrows.echo = false
                            }
                        }
                    }
                }

            }
        }
    }

    fun handleInquisWaypointReceived(pos: BlockPos, player: String, fromParty: Boolean = false) {
        if ((config.receiveInq == 2 || fromParty || MessageHandler.partyMembers.contains(player)) && !config.getIgnoreList().contains(player.lowercase())) {
            val waypoint = Waypoint.InquisWaypoint(pos, player, System.currentTimeMillis())
            Burrows.waypoints.add(waypoint)
            Utils.startTimerTask(config.inqWaypointTimeout.toLong()) { Burrows.waypoints.remove(waypoint) }
            Utils.showClientTitle(
                "",
                "§c" + player + " 's Inquis near " + Warp.closest(Vec3(pos))?.name
            )
            Utils.ping()
            val ignore = ChatComponentText("§c [Ignore this player] ")
            ignore.setChatStyle(
                ignore.getChatStyle().setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/diana ignore add $player"
                    )
                ).setChatHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText("ignore add $player")
                    )
                )
            )
            val component = ChatComponentText("$chatTitle§cInquis Waypoint received§r from $player ")
            component.appendSibling(ignore)
            Utils.modMessage(component)
            MessageHandler.receivedInquisFrom.add(player)
        }
    }
}