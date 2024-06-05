package diana.core

import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3

open class Waypoint (
    x: Double,
    y: Double,
    z: Double
): Vec3(x, y, z) {
    val blockPos = BlockPos(xCoord, yCoord, zCoord)
    constructor(pos: Vec3) : this(pos.xCoord, pos.yCoord, pos.zCoord)
    constructor(pos: BlockPos) : this(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

    override fun equals(other: Any?): Boolean {
        return if (other is Waypoint) {
            xCoord == other.xCoord && yCoord == other.yCoord && zCoord == other.zCoord
        } else super.equals(other)
    }

    class InquisWaypoint(
        pos: BlockPos,
        var player: String
    ) : Waypoint(pos) {
        override fun equals(other: Any?): Boolean {
            return if (other is InquisWaypoint) {
                xCoord == other.xCoord && yCoord == other.yCoord && zCoord == other.zCoord && player == other.player
            } else super.equals(other)
        }
    }

    class ParticleBurrowWaypoint(
        pos: BlockPos,
        var type: Int = -1 //1 start, 2 treasure, 3 footsteps, 4 enchants
    ) : Waypoint(pos) {
        fun setType(
            particle: S2APacketParticles
        ) = setType(
            particle.particleType,
            particle.particleCount,
            particle.particleSpeed,
            particle.xOffset,
            particle.yOffset,
            particle.zOffset
        )

        //from Skytils under the GNU 3.0 license
        private fun setType(
            particle: EnumParticleTypes,
            count: Int,
            speed: Float,
            xOffset: Float,
            yOffset: Float,
            zOffset: Float
        ) {
            if (particle == EnumParticleTypes.CRIT_MAGIC && count == 4 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f) {
                type = 1
            } else if (particle == EnumParticleTypes.CRIT && count == 3 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f) {
                type = 2
            } else if (particle == EnumParticleTypes.DRIP_LAVA && count == 2 && speed == 0.01f && xOffset == 0.35f && yOffset == 0.1f && zOffset == 0.35f) {
                type = 4
            } else if (particle == EnumParticleTypes.FOOTSTEP && count == 1 && speed == 0.0f && xOffset == 0.05f && yOffset == 0.0f && zOffset == 0.05f || particle == EnumParticleTypes.ENCHANTMENT_TABLE && count == 5 && speed == 0.05f && xOffset == 0.5f && yOffset == 0.4f && zOffset == 0.5f) {
                type = 3
            }
        }

        override fun equals(other: Any?): Boolean {
            return if (other is ParticleBurrowWaypoint) {
                xCoord == other.xCoord && yCoord == other.yCoord && zCoord == other.zCoord && type == other.type
            } else super.equals(other)
        }
    }
}