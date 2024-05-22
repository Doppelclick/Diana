package diana.gui

import diana.Diana.Companion.mc
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*


object GUIRenderUtils {
    private val tessellator: Tessellator = Tessellator.getInstance()
    val worldRenderer: WorldRenderer = tessellator.worldRenderer
    val fontRenderer: FontRenderer = mc.fontRendererObj

    fun renderRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x, y, w, h)
        tessellator.draw()

        GlStateManager.disableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun renderRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        GlStateManager.shadeModel(GL11.GL_FLAT)

        addQuadVertices(x - thickness, y, thickness, h)
        addQuadVertices(x - thickness, y - thickness, w + thickness * 2, thickness)
        addQuadVertices(x + w, y, thickness, h)
        addQuadVertices(x - thickness, y + h, w + thickness * 2, thickness)

        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
    }

    fun renderRoundedRect(x: Double, y: Double, w: Double, h: Double, color: Color, radius: Double) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        corner(x + w - radius, y + h - radius, radius, 0, 90)
        corner(x + w - radius, y + radius, radius, 90, 180)
        corner(x + radius, y + radius, radius, 180, 270)
        corner(x + radius, y + h - radius, radius, 270, 360)
        GL11.glEnd()

        GlStateManager.disableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun renderImage(resource: ResourceLocation, x: Int, y: Int, width: Int, height: Int, textureWidth: Float = width.toFloat(), textureHeight: Float = height.toFloat(), u: Float = 0f, v: Float = 0f) {
        GlStateManager.pushMatrix()
        GlStateManager.color(255f,255f,255f,255f)
        ConfigGui.mc.textureManager.bindTexture(resource)
        Gui.drawModalRectWithCustomSizedTexture(x, y, u, v, width, height, textureWidth, textureHeight)
        GlStateManager.popMatrix()
    }

    private fun addQuadVertices(x: Double, y: Double, w: Double, h: Double) {
        worldRenderer.pos(x, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()
    }

    private fun corner(x: Double, y: Double, radius: Double, start: Int, stop: Int) {
        var i = start
        while (i != stop) {
            i = min(stop.toDouble(), (i + 5).toDouble()).toInt()
            GL11.glVertex2d(
                (x + sin(i.toDouble() * Math.PI / 180.0) * radius),
                (y + cos(i.toDouble() * Math.PI / 180.0) * radius)
            )
        }
    }

    fun setUpScissorAbsolute(left: Int, top: Int, right: Int, bottom: Int, sc: Double = 1.0) {
        setUpScissor(left, top, (right - left).coerceAtLeast(0), (bottom - top).coerceAtLeast(0), sc)
    }

    fun setUpScissor(x: Int, y: Int, width: Int, height: Int, sc: Double = 1.0) {
        // Uses actual screen coordinates. Therefore scale:
        val scale = mc.displayHeight / ScaledResolution(mc).scaledHeight.toDouble() * sc
        GL11.glScissor(
            (x * scale).toInt(),
            (mc.displayHeight - (y + height) * scale).toInt(),
            (width * scale).toInt(),
            (height * scale).toInt()
        )
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
    }

    fun endScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    fun colorGradient(original: Color, new: Color, timeElapsed: Long, maxTime: Long, exponential: Boolean = true) : Color {
        if (timeElapsed == 0L) return original
        else if (timeElapsed >= maxTime) return new
        var perc = timeElapsed / maxTime.toFloat()

        if (exponential) perc = sqrt(perc)
        return Color(
            (original.red + (new.red - original.red) * perc) / 255f,
            (original.green + (new.green - original.green) * perc) / 255f,
            (original.blue + (new.blue - original.blue) * perc) / 255f,
            (original.alpha + (new.alpha - original.alpha) * perc) / 255f,
        )
    }

    fun FontRenderer.renderCentredScaledText(text: String, x: Float, y: Float, color: Color, scale: Float, shadow: Boolean = false) {
        renderScaledText(text, x - getStringWidth(text) / 2f * scale, y - FONT_HEIGHT / 2f * scale, color, scale, shadow)
    }

    fun FontRenderer.renderScaledText(text: String, x: Float, y: Float, color: Color, scale: Float, shadow: Boolean = false) {
        GlStateManager.scale(scale, scale, 1f)
        drawString(text, x / scale, y / scale, color.rgb, shadow)
        GlStateManager.scale(1 / scale, 1 / scale, 1f)
    }

    fun Color.fullAlpha() : Color {
        return Color(this.rgb, false)
    }
}