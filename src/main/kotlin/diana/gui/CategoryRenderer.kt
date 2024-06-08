package diana.gui

import diana.config.Category
import diana.gui.ConfigGui.InteractionFeedback
import diana.gui.ValueRenderer.Companion.getRenderer
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.roundToInt

open class CategoryRenderer(
    val category: Category
) {
    open val values = category.containedValues.map { it.getRenderer() }
    open val visibleValues
        get() = values.filter { it.value.notHidden() }.let {
            if (ConfigGui.searchBar.textInput.isNotEmpty())
                it.filter { it.value.let { it.name.contains(ConfigGui.searchBar.textInput, true) || it.printableDescription.contains(ConfigGui.searchBar.textInput, true) } }
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

        val translatedX = 450.0 + SIDES_OFFSET
        val translatedY = 140.0 + SIDES_OFFSET
        GUIRenderUtils.setUpScissor(translatedX.toInt(), translatedY.toInt(), 1010 - SIDES_OFFSET * 2, tabHeight, scale)
        GlStateManager.translate(translatedX, translatedY, 0.0)
        val offsetMouseX = mouseX.offsetMouseXForSetting()
        val offsetMouseY = mouseY.offsetMouseYForSetting()

        for (valueRenderer in visibleValues.reversed()) {
            valueRenderer.drawScreen(offsetMouseX, offsetMouseY, theme, scale, this, translatedX, translatedY)
        }
        GUIRenderUtils.endScissor()

        for (valueRenderer in visibleValues.reversed()) {
            valueRenderer.drawExpandedOptions(offsetMouseX, offsetMouseY, theme, scale, this, translatedX, translatedY)
        }

        GlStateManager.popMatrix()
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): InteractionFeedback {
        for (value in visibleValues.reversed()) {
            when (value.mouseClicked(mouseX.offsetMouseXForSetting(), mouseY.offsetMouseYForSetting(), mouseButton, this)) {
                InteractionFeedback.OPENED -> {
                    closeSettings(value)
                    return InteractionFeedback.OPENED
                }
                InteractionFeedback.INTERACTED -> {
                    return InteractionFeedback.INTERACTED
                }
                else -> {}
            }
        }
        return InteractionFeedback.NONE
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): InteractionFeedback {
        var re = InteractionFeedback.NONE
        for (value in visibleValues) {
            if (value.mouseReleased(mouseX.offsetMouseXForSetting(), mouseY.offsetMouseYForSetting(), state) == InteractionFeedback.CLOSED) {
                re = InteractionFeedback.CLOSED
            }
        }
        return re
    }

    fun keyTyped(typedChar: Char, keyCode: Int): InteractionFeedback {
        var re = InteractionFeedback.NONE
        for (value in visibleValues) {
            val feedback = value.keyTyped(typedChar, keyCode)
            if (InteractionFeedback.entries.indexOf(feedback) > InteractionFeedback.entries.indexOf(re)) {
                re = feedback
            }
        }
        return re
    }

    fun scroll(amount: Int, mouseX: Double, mouseY: Double) {
        for (value in visibleValues) {
            if (value.scroll(amount, mouseX.offsetMouseXForSetting(), mouseY.offsetMouseYForSetting(), this)) return
        }
        scroll += amount
    }

    fun closeSettings(exception: ValueRenderer? = null) {
        for (value in values.let { if (exception != null) it.minus(exception) else it }) {
            value.closeSetting()
        }
    }

    fun onResize() {
        scroll = scroll // Make sure it is in range
    }

    open fun onInit() {
        scroll = scroll
        for (value in values) {
            value.onInit()
        }
    }

    open fun onConfigReload() {
        for (value in values) {
            value.onConfigReload()
        }
    }

    private fun Double.offsetMouseXForSetting() = this - 450 - SIDES_OFFSET
    private fun Double.offsetMouseYForSetting() = this - 140 - SIDES_OFFSET

    companion object {
        const val BUTTON_OFFSET = 10
        const val BUTTON_HEIGHT = 50
        const val SPACED_BUTTON_HEIGHT = BUTTON_HEIGHT + BUTTON_OFFSET
        const val SCROLL_AMOUNT = 30f
        const val SIDES_OFFSET = 20
    }
}