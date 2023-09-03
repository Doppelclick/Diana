package diana.handlers

import diana.Diana.Companion.mc
import diana.config.Config
import diana.core.Burrows
import diana.core.Waypoint
import diana.events.PacketEvent
import diana.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PacketHandler {
    @SubscribeEvent
    fun onInboundPacket(event: PacketEvent.Inbound) {
        if (!Config.toggle) return
        if (event.packet is S02PacketChat) MessageHandler.onChatmessage(event.packet)
        else if (LocationHandler.doingDiana) {
            when (event.packet) {
                is S2APacketParticles -> {
                    val particle = event.packet
                    val pos = Vec3(particle.xCoordinate, particle.yCoordinate, particle.zCoordinate)
                    val down = BlockPos(pos).down()
                    if (particle.particleType == EnumParticleTypes.FIREWORKS_SPARK && particle.particleSpeed == 0f && particle.particleCount == 1 && Burrows.echo && Config.guess && particle.xOffset == 0f && particle.yOffset == 0f && particle.zOffset == 0f) {
                        Burrows.particles.add(pos)
                        Burrows.calcBurrow()
                    } else if (particle.particleType == EnumParticleTypes.REDSTONE && particle.particleSpeed == 1f && particle.particleCount == 0 && Burrows.arrow && Config.guess) {
                        if (Burrows.arrowStart == null) {
                            Burrows.arrowStart = pos
                        } else if (Burrows.arrowDir == null) {
                            val dir = pos.subtract(Burrows.arrowStart)
                            if (dir.xCoord == 0.0 && dir.zCoord == 0.0) return
                            Burrows.arrowDir = dir.normalize()
                            Burrows.arrow = false
                        }
                    } else if (Config.proximity && !Burrows.foundBurrows.contains(down) && !Burrows.dugBurrows.contains(down)) {
                        Burrows.waypoints.find { it.pos == down }?.takeIf { it is Waypoint.ParticleBurrowWaypoint }?.let {
                            (it as Waypoint.ParticleBurrowWaypoint).run {
                                if (it.type == 3) it.setType(particle)
                            }
                        } ?: run {
                            Burrows.waypoints.add(Waypoint.ParticleBurrowWaypoint(down).apply { this.setType(particle) }.apply {
                                if (this.type == -1) return
                                Utils.playSound(
                                    "note.pling",
                                    0.6f,
                                    1.2f
                                ) }
                            )
                        }
                    }
                }

                is S29PacketSoundEffect -> {
                    if (Config.guess && event.packet.soundName == "note.harp" && Burrows.echo) {
                        Burrows.sounds[Vec3(
                            event.packet.x,
                            event.packet.y,
                            event.packet.z)] = event.packet.pitch
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onOutboundPacket(event: PacketEvent.Outbound) {
        if (!Config.toggle || !LocationHandler.doingDiana || mc.thePlayer?.heldItem == null) return
        if (mc.thePlayer.heldItem.getDisplayName().contains("Ancestral Spade")) {
            when (event.packet) {
                is C07PacketPlayerDigging -> {
                    if (event.packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK && mc.theWorld?.getBlockState(event.packet.position)?.block == Blocks.grass) {
                        Burrows.dugBurrows.add(event.packet.position)
                    }
                }

                is C08PacketPlayerBlockPlacement -> {
                    if (event.packet.position != BlockPos(-1, -1, -1) && mc.theWorld?.getBlockState(event.packet.position)?.block == Blocks.grass) {
                        Burrows.dugBurrows.add(event.packet.position)
                    }
                }
            }
        }
    }
}