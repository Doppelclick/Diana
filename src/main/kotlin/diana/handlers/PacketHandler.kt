package diana.handlers

import diana.Diana.Companion.mc
import diana.config.categories.CategoryGeneral
import diana.core.Burrows
import diana.core.Waypoint
import diana.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.*
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3

object PacketHandler {
    fun onInboundPacket(packet: Packet<*>) {
        if (!CategoryGeneral.modToggled) return
        when (packet) {
            is S02PacketChat -> MessageHandler.onChatmessage(packet)
            is S3EPacketTeams -> LocationHandler.onScoreboard(packet)
            is S38PacketPlayerListItem -> LocationHandler.TabList.handlePacket(packet)
            else -> {
                if (!LocationHandler.doingDiana) return
                when (packet) {
                    is S2APacketParticles -> {
                        val pos = Vec3(packet.xCoordinate, packet.yCoordinate, packet.zCoordinate)
                        val down = BlockPos(pos).down()
                        if (packet.particleType == EnumParticleTypes.FIREWORKS_SPARK && packet.particleSpeed == 0f && packet.particleCount == 1
                            && Burrows.echo && CategoryGeneral.guess && packet.xOffset == 0f && packet.yOffset == 0f && packet.zOffset == 0f) {
                            Burrows.particles.add(pos)
                            Burrows.calcBurrow()
                        } else if (packet.particleType == EnumParticleTypes.REDSTONE && packet.particleSpeed == 1f && packet.particleCount == 0 && Burrows.arrow && CategoryGeneral.guess) {
                            if (Burrows.arrowStart == null) {
                                Burrows.arrowStart = pos
                            } else if (Burrows.arrowDir == null) {
                                val dir = pos.subtract(Burrows.arrowStart)
                                if (dir.xCoord == 0.0 && dir.zCoord == 0.0) return
                                Burrows.arrowDir = dir.normalize()
                                Burrows.arrow = false
                            }
                        } else if (CategoryGeneral.proximity && !Burrows.foundBurrows.contains(down) && !Burrows.dugBurrows.contains(down)) {
                            Burrows.waypoints.find { it.pos == down }?.takeIf { it is Waypoint.ParticleBurrowWaypoint }
                                ?.let {
                                    (it as Waypoint.ParticleBurrowWaypoint).run {
                                        if (it.type == 3) it.setType(packet)
                                    }
                                } ?: run {
                                Burrows.waypoints.add(
                                    Waypoint.ParticleBurrowWaypoint(down).apply { this.setType(packet) }.apply {
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
                        if (CategoryGeneral.guess && packet.soundName == "note.harp" && Burrows.echo) {
                            Burrows.sounds[Vec3(
                                packet.x,
                                packet.y,
                                packet.z
                            )] = packet.pitch
                        }
                    }
                }
            }
        }
    }

    fun onOutboundPacket(packet: Packet<*>) {
        if (!CategoryGeneral.modToggled || !LocationHandler.doingDiana || mc.thePlayer?.heldItem == null) return
        if (mc.thePlayer.heldItem.displayName.contains("Ancestral Spade")) {
            when (packet) {
                is C07PacketPlayerDigging -> {
                    if (packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK && mc.theWorld?.getBlockState(packet.position)?.block == Blocks.grass) {
                        Burrows.dugBurrows.add(packet.position)
                    }
                }

                is C08PacketPlayerBlockPlacement -> {
                    if (packet.position != BlockPos(-1, -1, -1) && mc.theWorld?.getBlockState(packet.position)?.block == Blocks.grass) {
                        Burrows.dugBurrows.add(packet.position)
                    }
                }
            }
        }
    }
}