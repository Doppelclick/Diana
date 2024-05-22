package diana.gui.values

import diana.config.Value
import diana.gui.*
import diana.gui.ConfigGui.InteractionFeedback
import diana.gui.GUIRenderUtils.fullAlpha
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.roundToInt

class ColorRenderer(
    override val value: Value<Color>,
) : ValueRenderer(value) {
    override val settingWidth: Double = settingHeight * 2.0
    override val settingPosX: Double = settingSpace - settingWidth - 30.0
    override val clickOutsideToClose: Boolean = false
    private var draggingHueBar = false
    private var draggingHueSquare = false
    private var hue: Float = 0f
    private var saturation: Float = 0f
    private var brightness: Float = 0f

    init {
        onConfigReload()
    }

    override fun renderSetting(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, translatedPosX: Double, translatedPosY: Double) {
        GUIRenderUtils.renderRoundedRect( // Maybe create a renderRoundedBorder function instead...
            settingPosX, settingPosY, settingWidth, settingHeight, theme.settingDarkColor(), settingHeight / 2.0
        )
        GUIRenderUtils.renderRoundedRect(
            settingPosX + HALF_SIDE_SPACING, settingPosY + HALF_SIDE_SPACING, settingWidth - SIDE_SPACING, settingHeight - SIDE_SPACING,
            Color(Color.HSBtoRGB(hue, saturation, brightness)),
            (settingHeight - SIDE_SPACING) / 2.0
        )
    }

    override fun renderSettingOptions(mouseX: Double, mouseY: Double, theme: Theme, scale: Double, parentCategory: CategoryRenderer, translatedPosX: Double, translatedPosY: Double) {
        if (!inputting) return
        GlStateManager.pushMatrix()
        GlStateManager.translate(settingPosX + settingWidth - HUE_CONTAINER_WIDTH, settingPosY - HUE_CONTAINER_HEIGHT - 5.0, 0.0)

        // Background
        GUIRenderUtils.renderRoundedRect(
            0.0,
            0.0,
            HUE_CONTAINER_WIDTH,
            HUE_CONTAINER_HEIGHT,
            theme.settingDarkColor().fullAlpha(),
            5.0
        )

        // Set color by mouse cursor position
        if (draggingHueSquare || draggingHueBar) {
            val hueY = mouseY - (settingPosY - HUE_CONTAINER_HEIGHT - 5.0 + HUE_SIZE_DIFF)
            if (draggingHueBar) {
                hue = (hueY / HUE_SIZE).coerceIn(0.0, 1.0).toFloat()
            }
            else {
                val hueX = mouseX - (settingPosX + settingWidth - HUE_CONTAINER_WIDTH + HUE_SIZE_DIFF)
                saturation = (hueY / HUE_SIZE).toFloat().coerceIn(0f, 1f)
                brightness = (1 - hueX / HUE_SIZE).toFloat().coerceIn(0f, 1f)
            }
        }

        GlStateManager.scale(1 / scale, 1 / scale, 1.0) // To ensure we render per pixel, not pixel * scale
        val hueSize = HUE_SIZE * scale
        val sizeDiff = HUE_SIZE_DIFF * scale

        // Hue Bar
        val barX = (HUE_SIZE + HUE_SIZE_DIFF * 2.0) * scale
        val barW = HUE_BAR_WIDTH * scale
        for (i in 0 until hueSize.roundToInt()) {
            val color = Color.getHSBColor(i / hueSize.toFloat(), 1f, 1f)
            GUIRenderUtils.renderRect(barX, sizeDiff + i, barW, 1.0, color)
        }

        // Hue Square -> Maybe better use lookup table (the same goes for the above)
        for (i in 0 until hueSize.roundToInt()) {
            for (j in 0 until hueSize.roundToInt()) {
                val saturation = j / hueSize.toFloat()
                val brightness = 1 - i / hueSize.toFloat()
                val color = Color.getHSBColor(hue, saturation, brightness)
                GUIRenderUtils.renderRect(sizeDiff + i, sizeDiff + j, 1.0, 1.0, color)
            }
        }
        GlStateManager.scale(scale, scale, 1.0)

        // Current Hue & SB
        GUIRenderUtils.renderRectBorder(HUE_SIZE + HUE_SIZE_DIFF * 2.0, HUE_SIZE_DIFF + HUE_SIZE * hue - 1.5, HUE_BAR_WIDTH, 3.0, 1.0, Color.WHITE)
        GUIRenderUtils.renderRectBorder(HUE_SIZE_DIFF + HUE_SIZE - (brightness * HUE_SIZE - 1) - 1.5, HUE_SIZE_DIFF + saturation * HUE_SIZE - 1.5, 3.0, 3.0, 1.0, Color.WHITE)

        GlStateManager.popMatrix()
    }

    override fun settingClicked(mouseX: Double, mouseY: Double, mouseButton: Int): InteractionFeedback {
        if (isSettingHovered(mouseX, mouseY)) {
            inputting = !inputting
            return if (inputting) InteractionFeedback.OPENED else InteractionFeedback.CLOSED
        } else if (inputting) {
            val hueX = mouseX - (settingPosX + settingWidth - HUE_CONTAINER_WIDTH + HUE_SIZE_DIFF)
            val hueY = mouseY - (settingPosY - HUE_CONTAINER_HEIGHT - 5.0 + HUE_SIZE_DIFF)
            if (hueX in 0.0..HUE_CONTAINER_WIDTH && hueY in 0.0..HUE_CONTAINER_HEIGHT) {
                if (hueX in 0.0.. HUE_SIZE && hueY in 0.0..HUE_SIZE) {
                    draggingHueSquare = true
                } else if (hueX in HUE_SIZE + HUE_SIZE_DIFF..HUE_SIZE + HUE_SIZE_DIFF + HUE_BAR_WIDTH && hueY in 0.0..HUE_SIZE) {
                    draggingHueBar = true
                }
                return InteractionFeedback.INTERACTED
            }
        }
        closeSetting()
        return InteractionFeedback.CLOSED
    }


    override fun settingMouseReleased(): InteractionFeedback {
        if (draggingHueBar || draggingHueSquare) {
            draggingHueBar = false
            draggingHueSquare = false
            value.set(Color(Color.HSBtoRGB(hue, saturation, brightness)))
            return InteractionFeedback.INTERACTED
        }
        return InteractionFeedback.NONE
    }

    override fun onConfigReload() {
        RGBtoHSB(value.value).let {
            hue = it[0]
            saturation = it[1]
            brightness = it[2]
        }
    }

    private fun RGBtoHSB(color: Color): FloatArray {
        return RGBtoHSB(color.red, color.green, color.blue)
    }

    private fun RGBtoHSB(r: Int, g: Int, b: Int): FloatArray {
        val hsbvals = FloatArray(3)
        var hue: Float
        val saturation: Float
        val brightness: Float
        val cmax = maxOf(r, g, b)
        val cmin = minOf(r, g, b)

        brightness = cmax.toFloat() / 255.0f
        saturation = if (cmax != 0) (cmax - cmin).toFloat() / cmax.toFloat() else 0f

        if (saturation == 0f) hue = 0f
        else {
            val redc = (cmax - r).toFloat() / (cmax - cmin).toFloat()
            val greenc = (cmax - g).toFloat() / (cmax - cmin).toFloat()
            val bluec = (cmax - b).toFloat() / (cmax - cmin).toFloat()
            hue = if (r == cmax) bluec - greenc
            else if (g == cmax) 2.0f + redc - bluec
            else 4.0f + greenc - redc
            hue /= 6.0f
            if (hue < 0) hue += 1.0f
        }
        hsbvals[0] = hue
        hsbvals[1] = saturation
        hsbvals[2] = brightness
        return hsbvals
    }

    companion object {
        const val HUE_SIZE = 150.0
        const val HUE_SIZE_DIFF = 10.0
        const val HUE_CONTAINER_HEIGHT = HUE_SIZE + HUE_SIZE_DIFF * 2.0
        const val HUE_BAR_WIDTH = 20.0
        const val HUE_CONTAINER_WIDTH = HUE_SIZE + HUE_SIZE_DIFF * 3.0 + HUE_BAR_WIDTH
    }
}