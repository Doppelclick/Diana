package diana.handlers

import diana.Diana.Companion.mc
import diana.Diana.Companion.waypointStyles
import diana.config.categories.CategoryGeneral
import diana.config.categories.CategoryRender
import diana.config.categories.CategorySelector
import diana.core.Burrows
import diana.core.Burrows.burrow
import diana.core.Burrows.waypoints
import diana.core.Waypoint
import diana.handlers.BurrowSelector.isSelected
import diana.handlers.BurrowSelector.selected
import diana.utils.RenderUtils
import diana.utils.Utils
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.lang.ref.WeakReference

object Render {
    private var lastBurrow: Vec3? = null
    private var lastLastBurrow: Vec3? = null
    private var scale = 1f
    private var scaleTime: Long = 0L
    private var lastInterpolation: Long = 0L
    private var interpolation: Long = 0L
    private var lastCheck = 0L

    var line1: Triple<Int, Int, Int>? = null
    var line2: Triple<Int, Int, Int>? = null
    var arrowLength: Int? = null

    fun Vec3.multiple(dist: Int): Vec3 {
        return this.normalize().let { Vec3(it.xCoord * dist, it.yCoord * dist, it.zCoord * dist) }
    }

    /**
     * Taken from DungeonRooms under Creative Commons Attribution-NonCommercial 3.0
     * https://github.com/Quantizr/DungeonRoomsMod/blob/3.x/LICENSE
     * @author Quantizr
     */
    @SubscribeEvent
    fun worldRender(event: RenderWorldLastEvent) { //TODO: check if beacons are visible at all and if not, don't render
        if (line1 != null && Burrows.particles.size - 1 >= line1!!.second) {
            val start = Burrows.particles[line1!!.first]
            val dir = Burrows.particles[line1!!.second].subtract(start)
            RenderUtils.draw3DLine(start, dir.multiple(line1!!.third).add(start), Color.white.rgb, 1, true, event.partialTicks)
        }
        if (line2 != null && Burrows.oldParticles.size - 1 >= line2!!.second) {
            val start = Burrows.oldParticles[line2!!.first]
            val dir = Burrows.oldParticles[line2!!.second].subtract(start)
            RenderUtils.draw3DLine(start, dir.multiple(line2!!.third).add(start), Color.blue.rgb, 1, true, event.partialTicks)
        }
        if (arrowLength != null && Burrows.arrowStart != null && Burrows.arrowDir != null) {
            val start = Burrows.arrowStart!!
            val dir = Burrows.arrowDir!!
            RenderUtils.draw3DLine(start, dir.multiple(arrowLength!!).add(start), Color.red.rgb, 1, true, event.partialTicks)
        }

        if (!CategoryGeneral.modToggled || (!CategoryRender.beaconBlock && !CategoryRender.beaconBeam && !CategoryRender.beaconText)) return //  || !LocationHandler.doingDiana
        var guessPos: Vec3? = null
        if (CategoryGeneral.guess && burrow != null) {
            if (CategoryRender.interpolation) {
                if (lastBurrow == null) lastBurrow = burrow
                if (lastLastBurrow == null) lastLastBurrow = lastBurrow
                guessPos = if (lastBurrow!!.distanceTo(lastLastBurrow!!) < 0.3) {
                    burrow
                } else {
                    val interpolationFactor =
                        (Math.round((System.currentTimeMillis() - interpolation).coerceAtMost(2000L) * 100f)
                                / 100f / (interpolation - lastInterpolation).coerceAtMost(2000L)).coerceIn(0f, 1f) // Maybe change 2000L
                    Vec3(
                        lastLastBurrow!!.xCoord + (lastBurrow!!.xCoord - lastLastBurrow!!.xCoord) * interpolationFactor,
                        lastLastBurrow!!.yCoord + (lastBurrow!!.yCoord - lastLastBurrow!!.yCoord) * interpolationFactor,
                        lastLastBurrow!!.zCoord + (lastBurrow!!.zCoord - lastLastBurrow!!.zCoord) * interpolationFactor
                    )
                }
                if (lastBurrow != burrow) {
                    lastInterpolation = interpolation
                    lastLastBurrow = lastBurrow
                    interpolation = System.currentTimeMillis()
                    lastBurrow = burrow
                }
            } else guessPos = burrow!!
        }

        if (CategorySelector.selectionMode == CategorySelector.SelectionModeChoice.HOVER) {
            if (lastCheck + 75 < System.currentTimeMillis()) {
                lastCheck = System.currentTimeMillis()
                selected =
                    waypoints.plus(
                        if (guessPos != null) listOf(Waypoint(guessPos))
                        else listOf()
                    ).minByOrNull {
                        Utils.visualDistanceTo(it, mc.thePlayer)
                    }?.takeIf {
                        Utils.visualDistanceTo(it, mc.thePlayer) < 16200 && it.distanceTo(mc.thePlayer.positionVector) > 7
                    }?.let {
                        WeakReference(it)
                    }
            }

            if (selected?.get() != null) {
                if (scaleTime == 0L) {
                    scaleTime = System.currentTimeMillis()
                } else {
                    scale = 1f + ((System.currentTimeMillis() - scaleTime).toFloat() / 250f).coerceIn(0f, 1f)
                }
            } else {
                scale = 1f
                scaleTime = 0
            }
        }

        if (CategoryGeneral.guess && guessPos != null) {
            try {
                RenderUtils.renderBeacon(
                    event.partialTicks,
                    "§l§bGuess " + waypointStyles[Burrows.lastDug]?.first + " (" + Burrows.lastDug + ")",
                    if (burrow?.isSelected() == true) scale else 1f,
                    guessPos,
                    if (CategoryRender.guessSeparateColor) CategoryRender.guessColor else (waypointStyles[Burrows.lastDug]?.second ?: Color.BLACK),
                    1.0
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Utils.modMessage("Hnnngh wendewing ewwow :C Pwease send log")
            }
        }
        waypoints.toSet().forEach { waypoint ->
            var display = ""
            var color = Color.BLACK
            if (waypoint is Waypoint.ParticleBurrowWaypoint && CategoryGeneral.proximity) {
                display = waypointStyles[waypoint.type]?.first + " (" + (if (waypoint.type == 3) "2/" else "") + waypoint.type + ")"
                color = waypointStyles[waypoint.type]?.second ?: Color.BLACK
            } else if (waypoint is Waypoint.InquisWaypoint) {
                if (MessageHandler.partyMembers.contains(waypoint.player)) display = "§9"
                display = display + waypoint.player + "§r's §6Inquisitor"
                color = CategoryRender.inquisitorColor
            }
            try {
                RenderUtils.renderBeacon(
                    event.partialTicks,
                    display,
                    if (waypoint.isSelected()) scale else 1f,
                    waypoint,
                    color
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Utils.modMessage("Hnnngh wendewing ewwow :C Pwease send log")
            }
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