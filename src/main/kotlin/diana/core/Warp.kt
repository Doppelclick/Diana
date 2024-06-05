package diana.core

import diana.Diana
import diana.Diana.Companion.warps
import diana.config.categories.CategoryGeneral
import diana.utils.Utils
import net.minecraft.util.Vec3


class Warp(var pos: Vec3, var name: String, var enabled: () -> Boolean) {
    fun warpTo() {
        Diana.mc.thePlayer.sendChatMessage("/warp $name")
        if (CategoryGeneral.notifications.contains(CategoryGeneral.NotificationChoice.WARPED)) Utils.modMessage("Warped to $name")
        lastwarp = name
    }

    companion object {
        var lastwarp = "undefined"
        fun closest(target: Vec3, check: Boolean = true): Warp? =
            warps.filter { it.enabled() || !check }.minByOrNull { target.distanceTo(it.pos) }
    }
}

