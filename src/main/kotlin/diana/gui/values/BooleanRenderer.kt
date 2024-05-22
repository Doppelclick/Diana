package diana.gui.values

import diana.config.Value
import diana.gui.*
import diana.gui.ConfigGui.InteractionFeedback

class BooleanRenderer(
    override val value: Value<Boolean>
) : ValueRenderer(value) {
    override val settingWidth: Double = settingHeight * 2.0
    override val settingPosX: Double = settingSpace - settingWidth - 30.0

    override fun renderSetting(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, translatedPosX: Double, translatedPosY: Double) {
        GUIRenderUtils.renderRoundedRect(
            settingPosX, settingPosY, settingWidth,
            settingHeight, if (value.value) theme.settingBrightColor().darker() else theme.settingDarkColor(), settingHeight / 2.0
        )
        GUIRenderUtils.renderRoundedRect(
            HALF_SIDE_SPACING + if (value.value) settingPosX + settingHeight else settingPosX,
            settingPosY + HALF_SIDE_SPACING, settingHeight - SIDE_SPACING, settingHeight - SIDE_SPACING,
            theme.settingBrightColor().let { if (isSettingHovered(mouseX, mouseY)) it.brighter() else it }, (settingHeight - SIDE_SPACING) / 2.0
        )
    }

    override fun settingClicked(mouseX: Double, mouseY: Double, mouseButton: Int): InteractionFeedback {
        if (isSettingHovered(mouseX, mouseY)) {
            value.value = !value.value
            return InteractionFeedback.INTERACTED
        }
        return InteractionFeedback.NONE
    }
}