package diana.gui.values

import diana.config.RangedValue
import diana.gui.*
import diana.gui.ConfigGui.InteractionFeedback
import diana.gui.GUIRenderUtils.fontRenderer
import kotlin.math.roundToInt

open class FloatRenderer(
    value: RangedValue<*>
) : ValueRenderer(value) {
    override val settingWidth: Double = settingSpace - 50.0
    override val settingPosX: Double = settingSpace - settingWidth - 30.0
    private val innerHeight = settingHeight - SIDE_SPACING

    @Suppress("UNCHECKED_CAST")
    private fun getCastedValue() = value as RangedValue<Float>
    val range = value.getFrom()..value.getTo()
    val rangeDiff = range.endInclusive - range.start


    open fun getValue(): Float {
        return getCastedValue().value
    }
    open fun setValue(v: Float) {
        getCastedValue().set(v)
    }
    open fun getValueString(): String {
        return ((getValue() * 100f).roundToInt() / 100f).toString()
    }

    override fun renderSetting(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, translatedPosX: Double, translatedPosY: Double) {
        if (inputting) {
            setValue((range.start + ((mouseX - settingPosX - innerHeight / 2.0) / (settingWidth - SIDE_SPACING - innerHeight)).coerceIn(0.0, 1.0) * (rangeDiff)).toFloat())
        }

        GUIRenderUtils.renderRoundedRect(
            settingPosX, settingPosY, settingWidth,
            settingHeight, theme.settingDarkColor(), settingHeight / 2.0
        )

        GUIRenderUtils.renderRoundedRect(settingPosX + HALF_SIDE_SPACING, settingPosY + HALF_SIDE_SPACING,
            settingHeight + (settingWidth - settingHeight) * getValue() / rangeDiff - SIDE_SPACING, innerHeight,
            theme.settingBrightColor().let { if (inputting || isSettingHovered(mouseX, mouseY)) it.brighter() else it }, innerHeight / 2.0
        )
    }

    override fun renderSettingOptions(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, parentCategory: CategoryRenderer, translatedPosX: Double, translatedPosY: Double) {
        if (inputting || isSettingHovered(mouseX, mouseY)) {
            val valueString = getValueString()
            val valueWidth = fontRenderer.getStringWidth(valueString)
            val valueHeight = fontRenderer.FONT_HEIGHT + 6.0
            val valueX = mouseX.coerceIn(settingPosX + settingHeight / 2.0, settingPosX + settingWidth - settingHeight / 2.0) - valueWidth / 2.0
            val valueY = settingPosY - fontRenderer.FONT_HEIGHT / 2.0 - 5 - valueHeight

            GUIRenderUtils.renderRoundedRect(valueX - 3, valueY - 3, valueWidth + 6.0, valueHeight, theme.settingDarkColor(), 3.0)
            fontRenderer.drawString(valueString, valueX.toFloat(), valueY.toFloat(), theme.valueNameColor().rgb, false)
        }
    }

    override fun settingClicked(mouseX: Double, mouseY: Double, mouseButton: Int): InteractionFeedback {
        if (isSettingHovered(mouseX, mouseY)) {
            inputting = true
            return InteractionFeedback.OPENED
        }
        return InteractionFeedback.NONE
    }

    override fun settingMouseReleased(): InteractionFeedback {
        if (inputting) {
            closeSetting()
            return InteractionFeedback.CLOSED
        }
        return InteractionFeedback.NONE
    }
}