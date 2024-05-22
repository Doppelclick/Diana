package diana.config.categories

import diana.config.Category
import diana.config.MultiChooseList
import diana.config.NamedChoice
import diana.config.toMultiChooseList
import diana.gui.Theme.Companion.ThemeChoice
import diana.soopy.WebsiteConnection

object CategoryGeneral : Category("General") {
    var modToggled by boolean("Toggle", true).listen {
        if (CategoryInquisitor.soopyServerOn()) {
            if (it) WebsiteConnection.connect(preConfigToggle = true)
            else WebsiteConnection.disconnect()
        }
        return@listen it
    }
    var theme by choice("Theme", ThemeChoice.DEFAULT, ThemeChoice.entries.toTypedArray()).hidden()
    var devMode by boolean("Dev Mode", false).hidden().doNotInclude()
    var updateCheckBeta by boolean("Check Beta Versions", true).apply { description = "Also check for beta versions when checking for updates." }
    var guess by boolean("Guess", true).apply { description = "Whether the mod should guess the location of the burrow." }
    var calculateIntercept by boolean("Intercept", true).apply { description = "Whether the mod should calculate an intercept as the burrow guess." }
    var interceptFullBlock by boolean("Intercept as full block", true).apply { description = "This will draw the burrow calculated with the intercept on a full block." }
    var accuracyChecks by boolean("Accuracy Checks", true).dev()
    var guessTolerance by float("Guess Tolerance", 5f, 0f..100f).dev().apply { description = "Tolerance for intercept calculation to prevent 'flickering'." }
    var proximity by boolean("Proximity detection", true).apply { description = "Detects nearby burrows." }
    var notifications by multiChoice("Notifications", MultiChooseList(), MessageChoice.entries.toMultiChooseList())

    enum class MessageChoice(override val choiceName: String, override val description: String? = null): NamedChoice {
        WARP("Warp available"), GUESS("Guess located"), WARPED("Warp completed")
    }
}