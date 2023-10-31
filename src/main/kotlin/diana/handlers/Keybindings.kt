package diana.handlers

import diana.Diana.Companion.config
import diana.Diana.Companion.mc
import diana.core.Burrows
import diana.core.Warp
import diana.utils.Utils
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard

object Keybindings {
    val keybindings = arrayOf(
        KeyBinding("Warp to selected burrow", Keyboard.KEY_NONE, "Diana")
    )
    @SubscribeEvent
    fun key(event: KeyInputEvent) {
        if (!config.toggle || !LocationHandler.doingDiana || mc.thePlayer == null) return
        if (keybindings[0].isPressed) {
            Warp.closest(Burrows.selected?: return)?.let { warp ->
                if (warp.pos.distanceTo(Burrows.selected) * 1.1 < mc.thePlayer.positionVector.distanceTo(Burrows.selected)) {
                    mc.thePlayer.sendChatMessage("/warp " + warp.name)
                    if (config.messages) Utils.modMessage("Warped to " + warp.name)
                    Warp.lastwarp = warp.name
                }
            }
        }
    }
}