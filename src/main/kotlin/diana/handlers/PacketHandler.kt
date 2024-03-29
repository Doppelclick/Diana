package diana.handlers

import diana.Diana.Companion.config
import diana.Diana.Companion.mc
import diana.core.Burrows
import diana.core.Waypoint
import diana.events.PacketEvent
import diana.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.*
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PacketHandler {
    @SubscribeEvent
    fun onInboundPacket(event: PacketEvent.Inbound) {
        if (!config.toggle) return
        when (event.packet) {
            is S02PacketChat -> MessageHandler.onChatmessage(event.packet)
            is S3EPacketTeams -> LocationHandler.onScoreboard(event.packet)
            is S38PacketPlayerListItem -> LocationHandler.TabList.handlePacket(event.packet)
            else -> {
                if (!LocationHandler.doingDiana) return
                when (event.packet) {
                    is S2APacketParticles -> {
                        val particle = event.packet
                        val pos = Vec3(particle.xCoordinate, particle.yCoordinate, particle.zCoordinate)
                        val down = BlockPos(pos).down()
                        if (particle.particleType == EnumParticleTypes.FIREWORKS_SPARK && particle.particleSpeed == 0f && particle.particleCount == 1
                            && Burrows.echo && config.guess && particle.xOffset == 0f && particle.yOffset == 0f && particle.zOffset == 0f) {
                            Burrows.particles.add(pos)
                            Burrows.calcBurrow()
                        } else if (particle.particleType == EnumParticleTypes.REDSTONE && particle.particleSpeed == 1f && particle.particleCount == 0 && Burrows.arrow && config.guess) {
                            if (Burrows.arrowStart == null) {
                                Burrows.arrowStart = pos
                            } else if (Burrows.arrowDir == null) {
                                val dir = pos.subtract(Burrows.arrowStart)
                                if (dir.xCoord == 0.0 && dir.zCoord == 0.0) return
                                Burrows.arrowDir = dir.normalize()
                                Burrows.arrow = false
                            }
                        } else if (config.proximity && !Burrows.foundBurrows.contains(down) && !Burrows.dugBurrows.contains(down)) {
                            Burrows.waypoints.find { it.pos == down }?.takeIf { it is Waypoint.ParticleBurrowWaypoint }
                                ?.let {
                                    (it as Waypoint.ParticleBurrowWaypoint).run {
                                        if (it.type == 3) it.setType(particle)
                                    }
                                } ?: run {
                                Burrows.waypoints.add(
                                    Waypoint.ParticleBurrowWaypoint(down).apply { this.setType(particle) }.apply {
                                        if (this.type == -1) return
                                        Utils.playSound(
                                            "note.pling",
                                            0.6f,
                                            1.2f
                                        )
                                    }
                                )
                            }
                        }
                    }

                    is S29PacketSoundEffect -> {
                        if (config.guess && event.packet.soundName == "note.harp" && Burrows.echo) {
                            Burrows.sounds[Vec3(
                                event.packet.x,
                                event.packet.y,
                                event.packet.z
                            )] = event.packet.pitch
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onOutboundPacket(event: PacketEvent.Outbound) {
        if (!config.toggle || !LocationHandler.doingDiana || mc.thePlayer?.heldItem == null) return
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