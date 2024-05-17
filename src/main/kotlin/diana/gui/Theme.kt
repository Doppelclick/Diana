package diana.gui

import diana.config.NamedChoice
import java.awt.Color

open class Theme {
    open fun backGroundColor() = Color(70,77,77,180)
    open fun hoverColor() = Color(100,100,100,255)
    open fun hoverTextColor() = Color.white


    open fun categoryListBackgroundColor() = Color(33,33,33,180)


    open fun categorySelectorColor() = Color(10,150,150,200)
    open fun categorySelectorHoveredColor() = Color(120,180,200,190)
    open fun categorySelectorTextColor() = Color.white
    open fun categorySelectorHighlightTextColor() = Color(170,170,170,255)
    open fun categorySelectorTextShadow() = false
    open fun categorySelectorScrollbarColor() = Color(255,255,255,180)


    open fun categoryBackgroundColor() = categoryListBackgroundColor()
    open fun categoryButtonColor() = Color(110,130,130,200)
    open fun categoryAccentColor() = categorySelectorColor()


    open fun valueNameColor() = Color.white
    open fun valueDescriptionColor() = Color(200,200,200,255)


    open fun searchBarBackground() = Color(0,0,0,150)
    open fun searchBarBorder() = Color(255, 255, 255, 100)
    open fun searchBarTextMarkedColor() = Color(255,255,255,100)
    open fun searchBarCursorColor() = Color.WHITE
    open fun searchBarTextColor() = Color.WHITE

    companion object {
        enum class ThemeChoice(val themeClass: Theme, override val choiceName: String, override val description: String? = null): NamedChoice {
            DEFAULT(Theme(), "Default")
        }
    }
}