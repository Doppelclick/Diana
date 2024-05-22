package diana.core

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import diana.Diana.Companion.mc
import diana.config.categories.CategoryGeneral
import diana.utils.Utils
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Burrows {
    private val hubData: Map<Int, Map<Int, Int>> = try {
        BufferedReader(InputStreamReader(mc.resourceManager.getResource(ResourceLocation("diana", "hubdata.json")).inputStream)).use { reader ->
            Gson().fromJson(reader, JsonObject::class.java).entrySet().associate {
                it.key.toInt() to it.value.getAsJsonObject().entrySet().associate { it.key.toInt() to it.value.asInt }
            }
        }
    } catch (e: JsonSyntaxException) {
        println("Error parsing Diana hub data")
        mapOf()
    } catch (e: JsonIOException) {
        println("Error reading Diana hub data.")
        mapOf()
    }
    var dugBurrows: ArrayList<BlockPos> = arrayListOf()
    var particles: ArrayList<Vec3> = arrayListOf()
    var oldParticles: ArrayList<Vec3> = arrayListOf()
    var sounds: LinkedHashMap<Vec3, Float> = linkedMapOf()
    var echo = false
    var arrow = false
    var arrowStart: Vec3? = null
    var arrowDir: Vec3? = null
    var clicked: Long = 0L

    var selected: Vec3? = null
    var burrow: Vec3?
        get() = interceptPos?.let { if (CategoryGeneral.interceptFullBlock) Vec3(BlockPos(it)) else it } ?: guessPos
        set(pos) {
            guessPos = pos
            interceptPos = pos
        }
    var guessPos: Vec3? = null
    var interceptPos: Vec3? = null
    var lastDug: Int = 1
    var waypoints: ArrayList<Waypoint> = arrayListOf()
    var foundBurrows: ArrayList<BlockPos> = arrayListOf()

    fun calcBurrow() {
        if (particles.size < 3 || sounds.size < 3) return

        val first: Vec3 = particles[0]
        val last2: Vec3 = particles[particles.size - 2]
        val last: Vec3 = particles.last()
        val lastSound: Vec3 = sounds.keys.last()
        val all = sounds.values.zipWithNext { a, b -> b - a }.average()

        val lineDist = last2.subtract(last).lengthVector()
        val distance = Math.E / all - first.subtract(lastSound).lengthVector()
        val changes = last.subtract(last2).let { Vec3(it.xCoord / lineDist, it.yCoord / lineDist, it.zCoord / lineDist) }

        val x = lastSound.xCoord + changes.xCoord * distance
        val z = lastSound.zCoord + changes.zCoord * distance
        val y = getHeight(round(x).toInt(), round(z).toInt()) ?: burrow?.yCoord ?: (lastSound.yCoord + changes.yCoord * distance)

        guessPos = Vec3(x, y, z)
        if (CategoryGeneral.notifications.contains(CategoryGeneral.MessageChoice.GUESS) && !echo) Utils.modMessage( //TODO: Fix !echo
            "[" + Math.round(x) + "," + Math.round(
                guessPos!!.yCoord
            ) + "," + Math.round(z) + "] " + Math.round(distance).toInt()
        )
        interceptPos?.distanceTo(guessPos)?.run {
            val relGuess = sqrt(guessPos!!.distanceTo(mc.thePlayer.positionVector))
            if (this > (relGuess + CategoryGeneral.guessTolerance)) {
                interceptPos = null
            }
        }

        intercept()
    }

    private fun intercept() {
        if (!CategoryGeneral.calculateIntercept) return
        val playerPos = mc.thePlayer?.positionVector ?: return

        val p1 = particles.first()
        val v1 = particles.last().subtract(particles.first())

        val p2: Vec3
        val v2: Vec3
        if (oldParticles.size > 3) {
            p2 = oldParticles.first()
            v2 = oldParticles.last().subtract(p2)
        } else if (arrowStart != null && arrowDir != null) {
            p2 = arrowStart!!
            v2 = arrowDir!!
        } else return
        val intercept = Utils.interceptDirection(p1, v1, p2, v2)?.let {
            it.addVector(0.0, getHeight(it.xCoord.roundToInt(), it.zCoord.roundToInt()) ?: burrow?.yCoord ?: 60.0, 0.0)
        } ?: return
        if (guessPos != null && CategoryGeneral.accuracyChecks) {
            val relGuess = sqrt(guessPos!!.distanceTo(playerPos))
            if (intercept.distanceTo(guessPos) > (relGuess + CategoryGeneral.guessTolerance)) {
                return
            }
        }
        interceptPos = intercept
    }

    private fun getHeight(x: Int, z: Int) : Double? {
        return x.let { listOf(it, it - 1, it + 1) }.map { hubData[it]}. //help me pls
        firstNotNullOfOrNull{ it }?.let { map ->
            z.let { listOf(it, it - 1, it + 1) }.map { map[it] }.
            firstNotNullOfOrNull { it }?.toDouble() }
    }

    fun resetBurrow() {
        sounds.clear()
        particles = arrayListOf()
    }
}