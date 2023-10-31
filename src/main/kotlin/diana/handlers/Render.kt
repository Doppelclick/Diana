package diana.handlers

import diana.Diana.Companion.config
import diana.Diana.Companion.mc
import diana.Diana.Companion.waypointStyles
import diana.core.Burrows
import diana.core.Burrows.burrow
import diana.core.Burrows.selected
import diana.core.Burrows.waypoints
import diana.core.Waypoint
import diana.utils.RenderUtils
import diana.utils.Utils
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Render {
    private var lastBurrow: Vec3? = null
    private var lastLastBurrow: Vec3? = null
    private var scale = 1f
    private var scaleTime: Long = 0L
    private var lastInterpolation: Long = 0L
    private var interpolation: Long = 0L
    private var lastCheck = 0L

    /**
     * Taken from DungeonRooms under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Quantizr/DungeonRoomsMod/blob/3.x/LICENSE
     * @author Quantizr
     */
    @SubscribeEvent
    fun worldRender(event: RenderWorldLastEvent) {
        if (!config.toggle || (!config.beaconBlock && !config.beaconBeam && !config.beaconText) || !LocationHandler.doingDiana) return
        var guessPos: Vec3? = null
        if (config.guess && burrow != null) {
            if (config.interpolation) {
                if (lastBurrow == null) lastBurrow = burrow
                if (lastLastBurrow == null) lastLastBurrow = lastBurrow
                guessPos = if (lastBurrow!!.distanceTo(lastLastBurrow!!) < 0.15) {
                    burrow
                } else {
                    val interpolationFactor =
                        (Math.round((System.currentTimeMillis() - interpolation) * 100f) / 100f / (interpolation - lastInterpolation)).coerceIn(0f, 1f)
                    Vec3(
                        lastLastBurrow!!.xCoord + (lastBurrow!!.xCoord - lastLastBurrow!!.xCoord) * interpolationFactor,
                        lastLastBurrow!!.yCoord + (lastBurrow!!.yCoord - lastLastBurrow!!.yCoord) * interpolationFactor,
                        lastLastBurrow!!.zCoord + (lastBurrow!!.zCoord - lastLastBurrow!!.zCoord) * interpolationFactor)
                }
                if (lastBurrow != burrow) {
                    lastInterpolation = interpolation
                    lastLastBurrow = lastBurrow
                    interpolation = System.currentTimeMillis()
                    lastBurrow = burrow
                }
            } else guessPos = burrow!!
        }

        if (lastCheck + 75 < System.currentTimeMillis()) { //TODO: possibly adjust timing
            lastCheck = System.currentTimeMillis()
            selected = waypoints.map { Vec3(it.pos) }.plus(
                if (config.guess && guessPos != null) listOf(guessPos)
                else listOf()
            ).minByOrNull { Utils.visualDistanceTo(it, mc.thePlayer) }?.takeIf { Utils.visualDistanceTo(it, mc.thePlayer) < 129600 && it.distanceTo(mc.thePlayer.positionVector) > 15 }?.let {
                if (it == guessPos) burrow else it
            }
        }

        if (selected != null) {
            if (scaleTime == 0L) {
                scaleTime = System.currentTimeMillis()
            } else {
                scale = 1f + ((System.currentTimeMillis() - scaleTime).toFloat() / 250f).coerceIn(0f, 1f)
            }
        } else {
            scale = 1f
            scaleTime = 0
        }

        if (config.guess && guessPos != null) {
            RenderUtils.renderBeacon(
                event.partialTicks,
                "§l§bGuess " + waypointStyles[Burrows.lastDug]?.first + " (" + Burrows.lastDug + ")",
                if (burrow == selected) scale else 1f,
                guessPos,
                if (config.guessSeparateColor) config.guessColor else (waypointStyles[Burrows.lastDug]?.second ?: Color.BLACK),
                1.0
            )
        }
        waypoints.toSet().forEach {
            var display = ""
            var color = Color.BLACK
            if (it is Waypoint.ParticleBurrowWaypoint && config.proximity) {
                display = waypointStyles[it.type]?.first + " (" + (if (it.type == 3) "2/" else "") + it.type + ")"
                color = waypointStyles[it.type]?.second ?: Color.BLACK
            } else if (it is Waypoint.InquisWaypoint) {
                if (MessageHandler.partyMembers.contains(it.player)) display = "§9"
                display = display + it.player + "§r's §6Inquisitor"
                color = config.inquisitorColor
            }
            RenderUtils.renderBeacon(event.partialTicks, display, if (it.pos == selected?.let { BlockPos(it) }) scale else 1f, Vec3(it.pos), color)
        }
    }

    fun resetRender() {
        scale = 1f
        scaleTime = 0
        lastBurrow = null
        lastLastBurrow = null
        lastInterpolation = 0
        interpolation = 0
    }
}