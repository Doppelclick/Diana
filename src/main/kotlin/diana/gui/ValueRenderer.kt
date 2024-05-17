package diana.gui

import diana.config.Value
import diana.gui.GUIRenderUtils.renderScaledText
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.roundToInt

open class ValueRenderer(
    val value: Value<*>
) {
    val width = 1010 - CategoryRenderer.SIDES_OFFSET * 2.0
    var inputting = false

    fun index(parentCategory: CategoryRenderer) = parentCategory.visibleValues.indexOf(this)
    fun yPosition(parentCategory: CategoryRenderer) = index(parentCategory) * CategoryRenderer.SPACED_BUTTON_HEIGHT + CategoryRenderer.SCROLL_AMOUNT * parentCategory.scroll

    fun drawScreen(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, parentCategory: CategoryRenderer) {
        if (!isVisible(parentCategory)) return
        GlStateManager.pushMatrix()
        val yPos = yPosition(parentCategory)
        GlStateManager.translate(0.0, yPos, 0.0)

        GUIRenderUtils.renderRoundedRect(0.0, 0.0, width, CategoryRenderer.BUTTON_HEIGHT.toDouble(), theme.categoryButtonColor(), 10.0)
        GUIRenderUtils.fontRenderer.renderScaledText(value.name, 8f, 7f, theme.valueNameColor(), 2f)

        var desc = value.description
        var len = GUIRenderUtils.fontRenderer.getStringWidth(desc) * 1.5f
        val maxWidth = width * 3f / 5f
        if (len > maxWidth) {
            len += GUIRenderUtils.fontRenderer.getStringWidth("...") * 1.5f
            desc = desc.dropLastWhile {
                len -= GUIRenderUtils.fontRenderer.getCharWidth(it) * 1.5f
                len > maxWidth
            } + "..."
            if (mouseY in yPos..yPos + CategoryRenderer.BUTTON_HEIGHT && mouseX in 0.0..maxWidth) {
                ConfigGui.setHoveredText(value.description)
            }
        }
        GUIRenderUtils.fontRenderer.renderScaledText(desc, 8f, 30f, theme.valueDescriptionColor(), 1.5f)

        GlStateManager.translate(maxWidth + 10.0, 0.0, 0.0)
        renderSetting(mouseX - maxWidth - 10.0, mouseY - yPos, width * 2.0 / 5.0 - 10.0, theme, scale)
        GlStateManager.popMatrix()
    }

    open fun renderSetting(mouseX: Double, mouseY: Double, width: Double, theme: Theme, scale: Double) {

    }

    open fun drawExpandedOptions(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, parentCategory: CategoryRenderer) {
        //if (!isVisible(parentCategory)) return
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return false
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, state: Int): Boolean {
        return false
    }

    // yPos + CategoryRenderer.BUTTON_HEIGHT >= 0 && yPos < parentCategory.tabHeight
    fun isVisible(parentCategory: CategoryRenderer) = yPosition(parentCategory).roundToInt() in -CategoryRenderer.BUTTON_HEIGHT..parentCategory.tabHeight

    open fun isHovered(): Boolean {
        return false
    }
}