package diana.core

import diana.Diana
import net.minecraft.util.Vec3


class Warp(var pos: Vec3, var name: String, var enabled: () -> Boolean) {
    companion object {
        var lastwarp = "undefined"
        fun closest(target: Vec3, check: Boolean): Warp? =
            Diana.warps.filter { it.enabled() || !check }.minByOrNull { target.distanceTo(it.pos) }
    }
}

