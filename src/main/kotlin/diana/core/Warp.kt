package diana.core

import diana.Diana
import diana.Diana.Companion.warps
import diana.config.categories.CategoryGeneral
import diana.utils.Utils
import net.minecraft.util.Vec3


class Warp(
    x: Double,
    y: Double,
    z: Double,
    val name: String,
    private val penalty: Double = 0.0,
    val enabled: () -> Boolean
) : Vec3(x, y, z) {
    override fun distanceTo(other: Vec3): Double {
        return realDistanceTo(other) + penalty
    }

    fun realDistanceTo(other: Vec3): Double {
        return super.distanceTo(other)
    }

    fun warpTo() {
        Diana.mc.thePlayer.sendChatMessage("/warp $name")
        if (CategoryGeneral.notifications.contains(CategoryGeneral.NotificationChoice.WARPED)) Utils.modMessage("Warped to $name")
        lastwarp = name
    }

    companion object {
        var lastwarp = "undefined"

        /**
         * @return The warp with its distance to the target position
         */
        fun closest(target: Vec3, check: Boolean = true): Pair<Warp, Double> =
            warps.filter { it.enabled() || !check }.map { it to it.distanceTo(target) }.minBy { it.second }
    }
}

