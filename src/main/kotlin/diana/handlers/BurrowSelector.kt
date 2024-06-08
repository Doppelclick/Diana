package diana.handlers

import diana.Diana
import diana.config.categories.CategoryGeneral
import diana.config.categories.CategorySelector
import diana.config.categories.CategorySelector.SelectionModeChoice
import diana.core.Burrows
import diana.core.Warp
import diana.core.Waypoint
import diana.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import java.lang.ref.WeakReference

object BurrowSelector {
    var warpNotification = false
    /** "Pointer" **/
    var selected: WeakReference<Waypoint?>? = null
    val currentWarp: Warp?
        get() {
            val loc: Waypoint =
                when (CategorySelector.selectionMode) {
                    SelectionModeChoice.HOVER -> {
                        selected?.get()
                    }

                    SelectionModeChoice.GUESS -> {
                        Burrows.burrow
                    }
            } ?: return null
            val warp = Warp.closest(loc)
            if (warp.second * 1.1 < Diana.mc.thePlayer.positionVector.distanceTo(loc)) {
                return warp.first
            }
            return null
        }

    fun sendWarpNotification() {
        if (warpNotification || !CategoryGeneral.notifications.contains(CategoryGeneral.NotificationChoice.WARP)) return
        if (currentWarp != null) {
            warpNotification = true
            Utils.showClientTitle(null, "Warp Available!")
        }
    }


    @SubscribeEvent
    fun onTick(event: InputEvent) {
        if (!CategoryGeneral.modToggled || !LocationHandler.doingDiana || Diana.mc.thePlayer == null) return
        if (CategorySelector.warpKeyBinding.mcKeyBinding.isPressed) {
            currentWarp?.warpTo()
        }
    }

    fun Waypoint.isSelected(hoverOnly: Boolean = true): Boolean {
        return when (CategorySelector.selectionMode) {
            SelectionModeChoice.HOVER -> {
                this == selected?.get()
            }

            SelectionModeChoice.GUESS -> {
                !hoverOnly && this == Burrows.burrow
            }
        }
    }
}