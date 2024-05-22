package diana.gui.values

import diana.config.*
import diana.gui.*
import diana.gui.GUIRenderUtils.renderScaledText
import java.awt.Color
import kotlin.math.roundToInt

@Suppress("UNCHECKED_CAST")
class ChoiceRenderer(
    override val value: Value<*>
) : ValueRenderer(value) {
    override val settingHeight: Double = CategoryRenderer.BUTTON_HEIGHT.toDouble()
    override val settingWidth: Double = settingSpace - 30.0 * 2.0
    override val settingPosX: Double = 30.0
    override val settingPosY: Double = 0.0
    private val textScale = (OPTIONS_BUTTON_HEIGHT - OPTIONS_DIFF * 2.0) / GUIRenderUtils.fontRenderer.FONT_HEIGHT

    private val hoverTimes = Array<Long?>(getChoices().size) { null }
    private val choiceButtonTexts = getChoices().map {
        it.choiceName to GUIRenderUtils.fontRenderer.getStringWidth(it.choiceName) * textScale
    }.toMap()
    private val descriptions = getChoices().map { it.description }.toTypedArray()

    // Scroll
    private val minScroll = ((settingWidth - (choiceButtonTexts.values.fold(0.0) { acc, size -> acc + (size + OPTIONS_DIFF * 2.0) } + (hoverTimes.size - 1) * OPTIONS_BUTTON_SPACING)) / OPTIONS_SCROLL_AMOUNT)
        .coerceAtMost(0.0)
    private var optionsScroll = 0.0
        set(value) {
            field = value.coerceIn(minScroll, 0.0)
        }


    private fun getChoices(): List<NamedChoice> {
        return if (value.valueType == ValueType.MULTI_CHOOSE) (value as MultiChooseListValue<*>).choices
            else (value as ChooseListValue<*>).choices.toList()
    }
    private fun getValue(): List<NamedChoice> {
        return if (value.valueType == ValueType.MULTI_CHOOSE) (value as MultiChooseListValue<NamedChoice>).value
            else listOf((value as ChooseListValue<*>).value)
    }
    private fun setValueByString(choice: String) {
        if (value.valueType == ValueType.MULTI_CHOOSE) (value as MultiChooseListValue<NamedChoice>).setByString(choice)
            else (value as ChooseListValue<*>).setByString(choice)
    }


    override fun renderSetting(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, translatedPosX: Double, translatedPosY: Double) {
        val selected = getValue().map { it.choiceName }
        val y = settingPosY + OPTIONS_Y
        var x = settingPosX + optionsScroll * OPTIONS_SCROLL_AMOUNT
        val withinParams = mouseX in settingPosX..settingPosX + settingWidth && mouseY in y..y + OPTIONS_BUTTON_HEIGHT

        GUIRenderUtils.setUpScissor(
            (translatedPosX + settingPosX).roundToInt(),
            (translatedPosY + settingPosY).roundToInt(),
            settingWidth.roundToInt(),
            settingHeight.roundToInt(),
            scale
        )

        choiceButtonTexts.entries.forEachIndexed { index, (text, len) ->
            val bWidth = OPTIONS_DIFF * 2.0 + len

            if (x in settingPosX - bWidth..settingPosX + settingWidth) {
                hoverTimes[index] = if (withinParams && mouseX in x..x + bWidth) {
                    if (descriptions[index] != null) ConfigGui.setHoveredText(descriptions[index]!!)
                    hoverTimes[index] ?: System.currentTimeMillis()
                } else null

                GUIRenderUtils.renderRoundedRect(
                    x, y, bWidth, OPTIONS_BUTTON_HEIGHT,
                    if (hoverTimes[index] != null) GUIRenderUtils.colorGradient(
                        theme.settingDarkColor(), theme.settingDarkColor().brighter(),
                        System.currentTimeMillis() - hoverTimes[index]!!, 500L
                    )
                    else theme.settingDarkColor(),
                    10.0
                )
                GUIRenderUtils.fontRenderer.renderScaledText(
                    text, (x + OPTIONS_DIFF).toFloat(), (y + OPTIONS_DIFF + 2).toFloat(),
                    if (selected.contains(text)) Color.WHITE else Color.GRAY,
                    textScale.toFloat()
                )
            } else {
                hoverTimes[index] = null
            }
            x += bWidth + OPTIONS_BUTTON_SPACING
        }

        GUIRenderUtils.endScissor()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int, parentCategory: CategoryRenderer): ConfigGui.InteractionFeedback {
        val x = mouseX - textWidth - 30.0
        val y = mouseY - yPosition(parentCategory)
        if (isSettingHovered(x, y)) {
            hoverTimes.indexOfFirst { it != null }.let {
                if (it != -1) {
                    setValueByString(choiceButtonTexts.keys.elementAt(it))
                }
            }
        }
        return ConfigGui.InteractionFeedback.NONE
    }

    override fun settingScrolled(amount: Int, mouseX: Double, mouseY: Double): Boolean {
        if (isSettingHovered(mouseX, mouseY)) {
            optionsScroll += amount
            return true
        }
        return false
    }

    companion object {
        const val OPTIONS_Y = 10.0
        const val OPTIONS_BUTTON_HEIGHT = CategoryRenderer.BUTTON_HEIGHT - 20.0
        const val OPTIONS_DIFF = 6.0
        const val OPTIONS_BUTTON_SPACING = 7.0
        const val OPTIONS_SCROLL_AMOUNT = 30.0
    }
}