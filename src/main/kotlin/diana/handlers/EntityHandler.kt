package diana.handlers

import diana.Diana.Companion.chatTitle
import diana.Diana.Companion.mc
import diana.config.categories.CategoryGeneral
import diana.config.categories.CategoryInquisitor
import diana.config.categories.CategoryInquisitor.ChatChoice
import diana.config.categories.CategoryInquisitor.InqModeChoice
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
        if (!CategoryGeneral.modToggled || CategoryInquisitor.sendMode == ChatChoice.NONE || (CategoryInquisitor.sendMode == ChatChoice.PARTY &&! MessageHandler.inParty) || !LocationHandler.doingDiana) return
        val entity = event.entity ?: return
        val player = Minecraft.getMinecraft().thePlayer ?: return
        if (entity.name.contains("Minos Inquisitor")) {
            if (Utils.maxDistance(player.positionVector, entity.positionVector) < 10 && Burrows.dugBurrows.any { it.distanceSq(entity.position) < 25 }) {
                val pos = BlockPos(entity.positionVector).down()
                if (CategoryInquisitor.inqMode.contains(InqModeChoice.SOOPY)) {
                    SoopyV2Server.sendInquisData(pos, MessageHandler.partyMembers)
                }

                if (CategoryInquisitor.inqMode.contains(InqModeChoice.CHAT)) {
                    val warp = Warp.closest(entity.positionVector, false)?.name ?: "nothing"
                    player.sendChatMessage(("/" + (if (CategoryInquisitor.sendMode == ChatChoice.ALL) "a" else "p") + "c [Diana] Inquis! [" + pos.x + "," + pos.y + "," + pos.z + "] at ⏣ " + LocationHandler.location) + " warp: " + warp)
                }
            }
        }
    }

    @SubscribeEvent
    fun interact(event: PlayerInteractEvent) {
        if (!CategoryGeneral.modToggled || !LocationHandler.inHub) return
        val player = mc.thePlayer ?: return
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && CategoryGeneral.guess) {
            if (System.currentTimeMillis() > Burrows.clicked + 3000) {
                if (player.heldItem?.displayName?.contains("Ancestral Spade") == true) {
                    Burrows.oldParticles = Burrows.particles
                    Burrows.resetBurrow()
                    Burrows.echo = true
                    LocationHandler.doingDiana = true
                    Burrows.clicked = System.currentTimeMillis()
                    Utils.startTimerTask(4501) {
                        if (CategoryGeneral.guess && Burrows.echo) {
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
        if (player != mc.thePlayer.name && LocationHandler.doingDiana && LocationHandler.inHub && CategoryInquisitor.receiveMode != ChatChoice.NONE
            && (CategoryInquisitor.receiveMode == ChatChoice.ALL || fromParty || MessageHandler.partyMembers.contains(player)) && !CategoryInquisitor.getIgnoreList().contains(player.lowercase())) {
            val waypoint = Waypoint.InquisWaypoint(pos, player)
            Burrows.waypoints.add(waypoint)
            Utils.startTimerTask(CategoryInquisitor.inqWaypointTimeout.toLong()) { Burrows.waypoints.remove(waypoint) }
            Utils.showClientTitle(
                "",
                "§c" + player + " 's Inquis near " + Warp.closest(Vec3(pos))?.name
            )
            Utils.ping()
            val ignore = ChatComponentText("§c [Ignore this player] ")
            ignore.setChatStyle(
                ignore.chatStyle.setChatClickEvent(
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