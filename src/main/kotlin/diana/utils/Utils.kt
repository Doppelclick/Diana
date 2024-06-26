package diana.utils

import diana.Diana.Companion.chatTitle
import diana.Diana.Companion.mc
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.ChatComponentText
import net.minecraft.util.Vec3
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

object Utils {

    fun modMessage(text: String) {
        modMessage(constructModMessage(text))
    }

    fun modMessage(text: ChatComponentText) {
        mc.thePlayer?.addChatMessage(text)
    }

    fun constructModMessage(text: String) : ChatComponentText = ChatComponentText("$chatTitle$text")

    fun showClientTitle(title: String?, subtitle: String?) {
        mc.ingameGUI.displayTitle(null, null, 2, 40, 2) //set timings
        mc.ingameGUI.displayTitle(null, subtitle, -1, -1, -1) //do subtitle
        mc.ingameGUI.displayTitle(title, null, -1, -1, -1) //do title
    }


    fun playSound(sound: String?, volume: Float, pitch: Float) {
        mc.thePlayer?.playSound(sound, volume, pitch)
    }

    fun ping() {
        playSound("note.pling", 1f, 0.6f)
        startTimerTask(180) { playSound("note.pling", 1f, 0.7f) }
        startTimerTask(360) { playSound("note.pling", 1f, 0.8f) }
    }

    fun visualDistanceTo(burrow: Vec3, player: EntityPlayerSP): Double {
        val playerp = player.positionVector.addVector(0.0, player.getEyeHeight().toDouble(), 0.0)
        val yaw = (player.rotationYaw % 360.0).positiveAngle()
        val pitch = player.rotationPitch

        val burrowY: Double = getYaw(playerp, burrow.addVector(0.5, 0.5, 0.5)).positiveAngle()
        val lowP: Double = getPitch(playerp, burrow.addVector(0.5, 1.0, 0.5))
        val topP: Double = getPitch(playerp, Vec3(burrow.xCoord + 0.5, 255.0, burrow.zCoord + 0.5))

        val yawDiff = (maxOf(yaw, burrowY) - minOf(yaw, burrowY)).let {
            if (it < 180) it
            else 360 - it
        }
        return if (yawDiff in 0.0..4.0 && pitch < lowP + 4 && pitch > topP) {
            yawDiff * abs(lowP - pitch)
        } else 16200.0
    }

    fun maxDistance(pos: Vec3, target: Vec3): Double = maxOf(
            abs(target.xCoord - pos.xCoord),
            abs(target.yCoord - pos.yCoord),
            abs(target.zCoord - pos.zCoord)
    )

    fun getYaw(playerPos: Vec3, point: Vec3): Double { //horizontal
        return atan2(playerPos.xCoord - point.xCoord, point.zCoord - playerPos.zCoord) * 180 / Math.PI
    }

    fun getPitch(playerPos: Vec3, point: Vec3): Double { //vertical, up is negative
        return atan2(
            playerPos.yCoord + 1 - point.yCoord,
            hypot(playerPos.xCoord - point.xCoord, playerPos.zCoord - point.zCoord)
        ) * 180 / Math.PI
    }

    private fun Double.positiveAngle(): Double {
        return if (this < 0) this + 360 else this
    }

    fun startTimerTask(delay: Long, action: () -> Unit) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                action()
            }
        }, delay)
    }

    fun intercept(first: Vec3, second: Vec3, third: Vec3, fourth: Vec3): Vec3? {
        return interceptDirection(first, second.subtract(first), third, fourth.subtract(third))
    }

    fun interceptDirection(base: Vec3, direction: Vec3, other: Vec3, otherDirection: Vec3): Vec3? {
        //The following calculation is from Synthesis made by Luna
        val a = direction.zCoord / direction.xCoord * base.xCoord - base.zCoord
        val b = otherDirection.zCoord / otherDirection.xCoord * other.xCoord - other.zCoord
        val x = ((a - b) / (direction.zCoord / direction.xCoord - otherDirection.zCoord / otherDirection.xCoord)).apply { if (this.isNaN()) return null }
        val z = (direction.zCoord / direction.xCoord * x - a).apply { if (this.isNaN()) return null }

        return Vec3(x, 0.0, z)
    }
}