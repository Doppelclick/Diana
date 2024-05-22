package diana.config.categories

import diana.config.Category
import diana.config.NamedChoice
import diana.config.toMultiChooseList
import diana.utils.Utils
import java.awt.Color

object CategoryTest : Category("TEST") {
    val bool by boolean("BOOL", false)
    val int by int("INT", 0, 0..10)
    val float by float("FLOAT", 0f, 0f..10f)
    val text by text("TEXT", "aBc _")
    val color by color("COLOR", Color.WHITE)
    val action by action("ACTION") { Utils.modMessage("Action performed") }
    private val choiceExample by choice("CHOICE", ChoiceExample.CHOICE1, ChoiceExample.entries.toTypedArray())
    private val multiChoiceExample by multiChoice("MULTICHOICE", ChoiceExample.entries.toMultiChooseList(), ChoiceExample.entries.toMultiChooseList())

    internal enum class ChoiceExample(override val choiceName: String, override val description: String?) : NamedChoice {
        CHOICE1("Choice1", "test description 1"), CHOICE2("Choice2", "test description 2"), CHOICE3("Choice3", "test description 3")
    }

    init {
        dev()
        doNotInclude()
        description = "This category is simply for testing settings"
    }
}