package diana.gui.values

import diana.config.KeyBindingValue
import diana.gui.CategoryRenderer
import diana.gui.ConfigGui.InteractionFeedback
import diana.gui.ConfigGui.mc
import diana.gui.GUIRenderUtils
import diana.gui.GUIRenderUtils.renderCentredScaledText
import diana.gui.Theme
import diana.gui.ValueRenderer
import org.lwjgl.input.Keyboard
import java.awt.Color

class KeyBindingRenderer(
    override val value: KeyBindingValue,
) : ValueRenderer(value) {
    override val settingWidth: Double = settingHeight * 3.0
    override val settingPosX: Double = settingSpace - settingWidth - 30.0
    var isDuplicate = false

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
        GUIRenderUtils.fontRenderer.renderCentredScaledText(
            if (inputting) "Listening" else value.keyName,
            (settingPosX + settingWidth / 2.0).toFloat(),
            (settingPosY + settingHeight / 2.0).toFloat(),
            if (isDuplicate &&! inputting) Color.RED else Color.WHITE,
            1.5f
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int, parentCategory: CategoryRenderer): InteractionFeedback {
        if (inputting) {
            value.set(mouseButton - 100)
            checkDuplicate()
            closeSetting()
            return InteractionFeedback.INTERACTED
        } else if (isSettingHovered(mouseX - textWidth - 30.0, mouseY - yPosition(parentCategory))) {
            inputting = true
            return InteractionFeedback.OPENED
        }
        return InteractionFeedback.NONE
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): InteractionFeedback {
        if (inputting) {
            inputting = false
            when {
                keyCode == Keyboard.KEY_NONE && typedChar > '\u0000' -> {
                    value.set(typedChar.code + 256)
                }
                keyCode == Keyboard.KEY_ESCAPE -> {
                    value.set(Keyboard.KEY_NONE)
                    isDuplicate = false
                    return InteractionFeedback.CLOSED
                }
                else -> {
                    value.set(keyCode)
                }
            }
            checkDuplicate()
            return InteractionFeedback.INTERACTED
        }
        return InteractionFeedback.NONE
    }

    override fun onInit() {
        checkDuplicate()
    }

    override fun onConfigReload() {
        checkDuplicate()
    }

    private fun checkDuplicate() {
        if (value.value != Keyboard.KEY_NONE) {
            for (keyBinding in mc.gameSettings.keyBindings) {
                if (keyBinding != value.mcKeyBinding && keyBinding.keyCode == value.value) {
                    isDuplicate = true
                    return
                }
            }
        }
        isDuplicate = false
    }
}