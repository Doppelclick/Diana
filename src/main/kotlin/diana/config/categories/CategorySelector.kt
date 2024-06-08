package diana.config.categories

import diana.config.Category
import diana.config.NamedChoice
import diana.handlers.BurrowSelector
import org.lwjgl.input.Keyboard

object CategorySelector : Category("Selector") {
    var selectionMode by choice("Selection Mode", SelectionModeChoice.HOVER, SelectionModeChoice.entries.toTypedArray()).apply { description = "Where to warp to on key press" }.listen {
        if (it != SelectionModeChoice.HOVER) BurrowSelector.selected = null
        it
    }
    var warpKeyBinding = keyBinding("Warp Keybind", Keyboard.KEY_NONE).apply { description = "Warp to selected burrow" }

    enum class SelectionModeChoice(override val choiceName: String, override val description: String? = null): NamedChoice {
        HOVER("Hovered"), GUESS("Guess Only")
    }
}