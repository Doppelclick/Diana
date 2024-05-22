package diana.gui.values

import diana.config.Action
import diana.config.ActionValue
import diana.gui.ConfigGui
import diana.gui.GUIRenderUtils
import diana.gui.Theme
import diana.gui.ValueRenderer

class ActionRenderer(
    override val value: ActionValue<Action>
) : ValueRenderer(value) {
    override val settingWidth: Double = settingHeight * 3.0
    override val settingPosX: Double = settingSpace - settingWidth - 30.0

    override fun renderSetting(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, translatedPosX: Double, translatedPosY: Double) {
        GUIRenderUtils.renderRoundedRect(
            settingPosX, settingPosY, settingWidth,
            settingHeight, theme.settingBrightColor().darker(), settingHeight / 2.0
        )
        GUIRenderUtils.renderRoundedRect(
            settingPosX + HALF_SIDE_SPACING,
            settingPosY + HALF_SIDE_SPACING, settingWidth - SIDE_SPACING, settingHeight - SIDE_SPACING,
            theme.settingBrightColor().let { if (isSettingHovered(mouseX, mouseY)) it.brighter() else it }, (settingHeight - SIDE_SPACING) / 2.0
        )
    }

    override fun settingClicked(mouseX: Double, mouseY: Double, mouseButton: Int): ConfigGui.InteractionFeedback {
        if (isSettingHovered(mouseX, mouseY)) {
            value.value()
            return ConfigGui.InteractionFeedback.INTERACTED
        }
        return ConfigGui.InteractionFeedback.NONE
    }
}