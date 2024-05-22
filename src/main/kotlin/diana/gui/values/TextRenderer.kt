package diana.gui.values

import diana.config.Value
import diana.gui.*
import diana.gui.ConfigGui.InteractionFeedback

class TextRenderer(
    override val value: Value<String>
) : ValueRenderer(value) {
    override val settingWidth: Double = settingSpace - 50.0
    override val settingPosX: Double = settingSpace - settingWidth - 30.0

    private val inputField = TextInputField(settingPosX, settingPosY, settingWidth, settingHeight, cornerRadius = 15.0)

    init {
        inputField.textInput = StringBuilder(value.value)
    }

    override fun renderSetting(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, translatedPosX: Double, translatedPosY: Double) {
        inputField.drawScreen(mouseX, mouseY, scale, theme, translatedPosX = translatedPosX, translatedPosY = translatedPosY)
    }

    override fun settingClicked(mouseX: Double, mouseY: Double, mouseButton: Int): InteractionFeedback {
        return inputField.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun settingMouseReleased(): InteractionFeedback {
        return inputField.mouseReleased()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): InteractionFeedback {
        if (inputField.keyTyped(typedChar, keyCode) == InteractionFeedback.INTERACTED) {
            value.set(inputField.textInput.toString())
            return InteractionFeedback.INTERACTED
        }
        return InteractionFeedback.NONE
    }

    override fun settingScrolled(amount: Int, mouseX: Double, mouseY: Double): Boolean {
        return inputField.scrolled(amount, mouseX, mouseY)
    }

    override fun onConfigReload() {
        inputField.textInput = StringBuilder(value.value)
    }

    override fun closeSetting() {
        super.closeSetting()
        inputField.isFocused = false
    }
}