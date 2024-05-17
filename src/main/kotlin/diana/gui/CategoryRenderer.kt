package diana.gui

import diana.config.Category
import diana.config.Visibility
import diana.config.categories.CategoryGeneral
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.roundToInt

open class CategoryRenderer(
    val category: Category
) {
    open val values = category.containedValues.mapIndexed { index, value -> ValueRenderer(value) }
    open val visibleValues
        get() = values.filter { it.value.visibility.let { it == Visibility.VISIBLE || it == Visibility.DEV && CategoryGeneral.devMode } }.let {
            if (SearchBar.searchInput.isNotEmpty())
                it.filter {  it.value.let { it.name.contains(SearchBar.searchInput, true) || it.description.contains(SearchBar.searchInput, true) } }
            else it
        }
    var hoveredTime: Long? = null
    var scroll: Double = 0.0
        set(value) {
            field = value.coerceIn(minScroll, 0.0)
        }
    private val minScroll
        get() = (((ConfigGui.scaledHeight.toInt() - 280 - SIDES_OFFSET * 2.0) - (visibleValues.size * SPACED_BUTTON_HEIGHT - BUTTON_OFFSET)) / SCROLL_AMOUNT).coerceAtMost(0.0)

    val tabHeight
        get() = ConfigGui.scaledHeight.roundToInt() - 280 - SIDES_OFFSET * 2

    open fun drawScreen(mouseX: Double, mouseY: Double, theme: Theme, scale: Double) {
        GUIRenderUtils.renderRoundedRect(450.0, 140.0, 1010.0, ConfigGui.scaledHeight - 280.0, theme.categoryBackgroundColor(), 10.0)
        GlStateManager.pushMatrix()

        val mScroll = minScroll // scroll is negative
        val maxBarHeight = ConfigGui.scaledHeight - 280.0 - SIDES_OFFSET * 2.0
        val barHeight =
            if (mScroll < 0) (maxBarHeight * (visibleValues.size + (mScroll / SPACED_BUTTON_HEIGHT * SCROLL_AMOUNT)) / visibleValues.size) // (Total - Amount of categories that exceed available space) / Total
            else maxBarHeight // max Height * percentage visible
        val sbX = 1460.0 - 7.0 - 3.0
        val sbY = 140.0 + SIDES_OFFSET + if (mScroll < 0) (maxBarHeight - barHeight) * (scroll / mScroll) else 0.0
        GUIRenderUtils.renderRect(sbX, sbY, 5.0, barHeight, theme.categorySelectorScrollbarColor())

        GUIRenderUtils.setUpScissor(450 + SIDES_OFFSET, 140 + SIDES_OFFSET, 1010 - SIDES_OFFSET * 2, tabHeight, scale)
        GlStateManager.translate(450.0 + SIDES_OFFSET, 140.0 + SIDES_OFFSET, 0.0)
        val offsetMouseX = mouseX - 450 - SIDES_OFFSET
        val offsetMouseY = mouseY - 140 - SIDES_OFFSET

        for (valueRenderer in visibleValues.reversed()) {
            valueRenderer.drawScreen(offsetMouseX, offsetMouseY, theme, scale, this)
        }
        GUIRenderUtils.endScissor()

        for (valueRenderer in visibleValues.reversed()) {
            valueRenderer.drawExpandedOptions(offsetMouseX, offsetMouseY, theme, scale, this)
        }

        GlStateManager.popMatrix()
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        return false
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        return false
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return false
    }

    fun scroll(amount: Int): Boolean {
        scroll += amount
        return true
    }

    fun onResize() {
        scroll = scroll // Make sure it is in range
    }

    open fun onInit() {
        scroll = scroll
    }

    companion object {
        const val BUTTON_OFFSET = 10
        const val BUTTON_HEIGHT = 50
        const val SPACED_BUTTON_HEIGHT = BUTTON_HEIGHT + BUTTON_OFFSET
        const val SCROLL_AMOUNT = 30f
        const val SIDES_OFFSET = 20
    }
}