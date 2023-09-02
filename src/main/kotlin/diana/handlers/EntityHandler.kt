package diana.handlers

import diana.Diana
import diana.config.Config
import diana.core.Burrows
import diana.core.Warp
import diana.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EntityHandler {
    @SubscribeEvent
    fun entity(event: EntityJoinWorldEvent) {
        if (!Config.toggle || Config.sendInq == 0 || !LocationHandler.doingDiana) return
        val entity = event.entity ?: return
        val player = Minecraft.getMinecraft().thePlayer ?: return
        if (entity.name.contains("Minos Inquisitor") && (Config.sendInq == 2 || MessageHandler.inParty)) {
            if (Utils.maxDistance(player.positionVector, entity.positionVector) < 10) {
                val pos = BlockPos(entity.positionVector).down()
                val warp = Warp.closest(entity.positionVector, false)?.name ?: "nothing"
                player.sendChatMessage(("/" + (if (Config.sendInq == 2) "a" else "p") + "c [Diana] Inquis! [" + pos.x + "," + pos.y + "," + pos.z + "] at ⏣ " + LocationHandler.getLocation()) + " warp: " + warp)
            }
        }
    }

    @SubscribeEvent
    fun interact(event: PlayerInteractEvent) {
        if (!Config.toggle || !LocationHandler.inHub) return
        val player = Diana.mc.thePlayer ?: return
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && Config.guess) {
            if (System.currentTimeMillis() > Burrows.clicked + 3000) {
                if (player.heldItem?.getDisplayName()?.contains("Ancestral Spade") == true) {
                    Burrows.oldParticles = Burrows.particles
                    Burrows.resetBurrow()
                    Burrows.echo = true
                    LocationHandler.doingDiana = true
                    Burrows.clicked = System.currentTimeMillis()
                }

            }
        }
    }
}