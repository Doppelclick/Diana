package diana.config.categories

import diana.config.Category
import diana.config.NamedChoice

object CategorySelector : Category("Selector") {
    var selectionMode by choice("Selection Mode", SelectionModeChoice.HOVER, SelectionModeChoice.entries.toTypedArray()).apply { description = "Where to warp to on key press" }

    enum class SelectionModeChoice(override val choiceName: String, override val description: String? = null): NamedChoice {
        HOVER("Hovered"), GUESS("Guess Only")
    }
}