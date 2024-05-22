package diana.gui

import diana.config.NamedChoice
import java.awt.Color

open class Theme {
    /** Config Gui background **/
    open fun backGroundColor() = Color(70,77,77,180)

    // Hovered description
    open fun hoverColor() = Color(100,100,100,255)
    open fun hoverTextColor() = Color.white


    // Category selector window (left)
    open fun categoryListBackgroundColor() = Color(33,33,33,180)


    open fun categorySelectorColor() = Color(10,150,150,200)
    open fun categorySelectorHoveredColor() = Color(120,180,200,190)
    open fun categorySelectorTextColor() = Color.white
    open fun categorySelectorHighlightTextColor() = Color(170,170,170,255)
    open fun categorySelectorTextShadow() = false
    open fun categorySelectorScrollbarColor() = Color(255,255,255,180)


    // Category values window (right)
    open fun categoryBackgroundColor() = categoryListBackgroundColor()
    open fun categoryButtonColor() = Color(110,130,130,200)
    open fun settingBrightColor() = categorySelectorColor()
    open fun settingDarkColor() = categorySelectorHoveredColor()


    // Value in above window
    open fun valueNameColor() = Color.white
    open fun valueDescriptionColor() = Color(200,200,200,255)


    // Text fields (e.g. search bar)
    open fun textFieldBackground() = Color(0,0,0,150)
    open fun textFieldBorder() = Color(255, 255, 255, 100)
    open fun textFieldTextMarkedColor() = Color(255,255,255,100)
    open fun textFieldCursorColor() = Color.WHITE
    open fun textFieldTextColor() = Color.WHITE

    companion object {
        enum class ThemeChoice(val themeClass: Theme, override val choiceName: String, override val description: String? = null): NamedChoice {
            DEFAULT(Theme(), "Default")
        }
    }
}