package diana.gui

import diana.config.*
import diana.gui.ConfigGui.InteractionFeedback
import diana.gui.GUIRenderUtils.renderScaledText
import diana.gui.values.*
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.roundToInt

open class ValueRenderer(
    open val value: Value<*>
) {
    private val width = 1010 - CategoryRenderer.SIDES_OFFSET * 2.0
    val textWidth = width * 3f / 5f
    val settingSpace = width * 2.0 / 5.0 - 10.0
    open val settingHeight: Double = CategoryRenderer.BUTTON_HEIGHT - 20.0
    open val settingWidth: Double = 0.0
    open val settingPosX: Double = 0.0
    open val settingPosY: Double = (CategoryRenderer.BUTTON_HEIGHT - settingHeight) / 2.0
    open val clickOutsideToClose = true
    var inputting = false

    fun index(parentCategory: CategoryRenderer) = parentCategory.visibleValues.indexOf(this)
    fun yPosition(parentCategory: CategoryRenderer) = index(parentCategory) * CategoryRenderer.SPACED_BUTTON_HEIGHT + CategoryRenderer.SCROLL_AMOUNT * parentCategory.scroll

    fun drawScreen(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, parentCategory: CategoryRenderer, translatedPosX: Double, translatedPosY: Double) {
        if (!isVisible(parentCategory)) {
            closeSetting()
            return
        }
        GlStateManager.pushMatrix()
        val yPos = yPosition(parentCategory)
        GlStateManager.translate(0.0, yPos, 0.0)

        GUIRenderUtils.renderRoundedRect(0.0, 0.0, width, CategoryRenderer.BUTTON_HEIGHT.toDouble(), theme.categoryButtonColor(), 10.0)
        GUIRenderUtils.fontRenderer.renderScaledText(value.name, 8f, 7f, theme.valueNameColor(), 2f)

        var desc = value.description
        var len = GUIRenderUtils.fontRenderer.getStringWidth(desc) * 1.5f
        if (len > textWidth) {
            len += GUIRenderUtils.fontRenderer.getStringWidth("...") * 1.5f
            desc = desc.dropLastWhile {
                len -= GUIRenderUtils.fontRenderer.getCharWidth(it) * 1.5f
                len > textWidth
            } + "..."
            if (mouseY in yPos..yPos + CategoryRenderer.BUTTON_HEIGHT && mouseX in 0.0..textWidth) {
                ConfigGui.setHoveredText(value.description)
            }
        }
        GUIRenderUtils.fontRenderer.renderScaledText(desc, 8f, 30f, theme.valueDescriptionColor(), 1.5f)

        GlStateManager.translate(textWidth + 30.0, 0.0, 0.0)
        renderSetting(mouseX - textWidth - 30.0, mouseY - yPos, theme, scale, translatedPosX + textWidth + 30.0, translatedPosY + yPos)
        GlStateManager.popMatrix()
    }

    open fun renderSetting(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, translatedPosX: Double, translatedPosY: Double) {}

    fun drawExpandedOptions(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, parentCategory: CategoryRenderer, translatedPosX: Double, translatedPosY: Double) {
        if (!isVisible(parentCategory)) return

        GlStateManager.pushMatrix()
        val yPos = yPosition(parentCategory)
        GlStateManager.translate(textWidth + 30.0, yPos, 0.0)

        renderSettingOptions(mouseX - textWidth - 30.0, mouseY - yPos, theme, scale, parentCategory,
            translatedPosX + textWidth + 30.0, translatedPosY + yPos)

        GlStateManager.popMatrix()
    }

    open fun renderSettingOptions(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, parentCategory: CategoryRenderer, translatedPosX: Double, translatedPosY: Double) {}

    /** Not translated coords **/
    open fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int, parentCategory: CategoryRenderer): InteractionFeedback {
        val x = mouseX - textWidth - 30.0
        val y = mouseY - yPosition(parentCategory)
        if (clickOutsideToClose && !isSettingHovered(x, y)) {
            closeSetting()
            return InteractionFeedback.CLOSED
        }
        return settingClicked(x, y, mouseButton)
    }

    /** Translated coords **/
    open fun settingClicked(mouseX: Double, mouseY: Double, mouseButton: Int): InteractionFeedback {
        return InteractionFeedback.NONE
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): InteractionFeedback {
        return settingMouseReleased()
    }

    open fun settingMouseReleased(): InteractionFeedback {
        return InteractionFeedback.NONE
    }

    open fun scroll(amount: Int, mouseX: Double, mouseY: Double, parentCategory: CategoryRenderer): Boolean {
        return settingScrolled(amount, mouseX - textWidth - 30.0, mouseY - yPosition(parentCategory))
    }

    open fun settingScrolled(amount: Int, mouseX: Double, mouseY: Double): Boolean {
        return false
    }

    open fun keyTyped(typedChar: Char, keyCode: Int): InteractionFeedback {
        return InteractionFeedback.NONE
    }

    open fun closeSetting() {
        inputting = false
    }

    open fun onConfigReload() {}

    // yPos + CategoryRenderer.BUTTON_HEIGHT >= 0 && yPos < parentCategory.tabHeight
    fun isVisible(parentCategory: CategoryRenderer) = yPosition(parentCategory).roundToInt() in -CategoryRenderer.BUTTON_HEIGHT..parentCategory.tabHeight


    /** Provided mouse coordinates should be relative to setting **/
    open fun isSettingHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX in settingPosX..settingPosX + settingWidth && mouseY in settingPosY..settingPosY + settingHeight
    }

    companion object {
        const val SIDE_SPACING = 5.0
        const val HALF_SIDE_SPACING = SIDE_SPACING / 2.0

        @Suppress("UNCHECKED_CAST")
        fun Value<*>.getRenderer() : ValueRenderer {
            return when (this.valueType) {
                ValueType.BOOLEAN -> BooleanRenderer(this as Value<Boolean>)
                ValueType.INT -> IntRenderer(this as RangedValue<Int>)
                ValueType.FLOAT -> FloatRenderer(this as RangedValue<Float>)
                ValueType.TEXT -> TextRenderer(this as Value<String>)
                ValueType.COLOR -> ColorRenderer(this as Value<Color>)
                ValueType.ACTION -> ActionRenderer(this as ActionValue<Action>)
                ValueType.CHOOSE, ValueType.MULTI_CHOOSE -> ChoiceRenderer(this)
                else -> {
                    ValueRenderer(this)
                }
            }
        }
    }
}