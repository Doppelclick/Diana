package diana.gui

import diana.Diana
import diana.config.Category
import diana.config.Visibility
import diana.config.categories.CategoryGeneral
import diana.gui.GUIRenderUtils.renderScaledText
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import kotlin.math.roundToInt

object ConfigGui: GuiScreen() {
    private val configSystem = Diana.configSystem
    private val categoryRenderers = configSystem.categories.map { CategoryRenderer(it) }
    val visibleCategoryRenderers: List<CategoryRenderer>
        get() {
            return categoryRenderers.filter { it.category.visibility.let { it == Visibility.VISIBLE || it == Visibility.DEV && CategoryGeneral.devMode } }
        }
    private var openedCategory: CategoryRenderer = visibleCategoryRenderers.first()
    private var scale = 1.0
    var scaledHeight = 900.0
    private val theme
        get() = CategoryGeneral.theme.themeClass
    private var hoverText: List<String> = listOf()
    private var searchCategory = false
    val searchBar = TextInputField(1460.0 - 300.0, 40.0, 300.0, 40.0, searchField = true)

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        hoverText = listOf()

        val scaledResolution = ScaledResolution(mc)
        scale = scaledResolution.scaledWidth_double / 1600.0
        scaledHeight = scaledResolution.scaledHeight_double / scale
        GlStateManager.pushMatrix()

        GUIRenderUtils.renderRect(0.0,0.0, scaledResolution.scaledWidth_double, scaledResolution.scaledHeight_double, theme.backGroundColor()) // Background
        GlStateManager.scale(scale, scale, 1.0)

        // Render header
        val titleScale = (70 - 14) / fontRendererObj.FONT_HEIGHT.toFloat()
        val dianaWidth = fontRendererObj.getStringWidth("Diana") * titleScale
        val dianaVersionScale = titleScale * 0.3f
        GUIRenderUtils.renderRoundedRect(140.0, 20.0, 70.0 + dianaWidth + 4 + fontRendererObj.getStringWidth(Diana.version) * dianaVersionScale + 10, 70.0, theme.categoryBackgroundColor(), 10.0)
        fontRendererObj.renderScaledText("Diana", 140f + 70f, 20f + 7f + (70f - fontRendererObj.FONT_HEIGHT * titleScale) / 2f, theme.settingBrightColor(), titleScale)
        fontRendererObj.renderScaledText(Diana.version, 140f + 70f + dianaWidth + 7f, 20f + 70f - 7f - fontRendererObj.FONT_HEIGHT * dianaVersionScale, theme.settingDarkColor(), dianaVersionScale)
        GUIRenderUtils.renderImage(Diana.icon, 140 + 7, 20 + 7, 70 - 14, 70 - 14)

        // Render menus
        CategoryTab.drawCategoryList()
        openedCategory.drawScreen(mouseX / scale, mouseY / scale, theme, scale)
        searchBar.drawScreen(mouseX / scale, mouseY / scale, scale, theme)

        if (hoverText.isNotEmpty()) {
            val height = 10 + hoverText.size * (fontRendererObj.FONT_HEIGHT + 1) * 2.0
            val width = hoverText.maxOf { fontRendererObj.getStringWidth(it) }.toDouble() * 2.0
            val hx = mouseX / scale
            val xPos = hx - if (hx in 0.0..1000.0) 0.0 else (width - 10.0)
            val hy = mouseY / scale - height
            GUIRenderUtils.renderRoundedRect(
                xPos,
                hy,
                10 + width,
                height,
                theme.hoverColor(),
                5.0
            )
            hoverText.forEachIndexed { index, s ->
                fontRendererObj.renderScaledText(
                    s,
                    xPos.toFloat() + 5f,
                    hy.toFloat() + 5 + index * (fontRendererObj.FONT_HEIGHT + 1) * 2f,
                    theme.hoverTextColor(),
                    2f
                )
            }
        }

        GlStateManager.popMatrix()
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val s = Mouse.getEventDWheel().coerceIn(-1..1)
        if (s == 0) return
        val x = scaledMouseX()
        val y = scaledMouseY()
        when (inArea(x, y)) {
            1 -> {
                CategoryTab.scroll += s
            }
            2 -> {
                openedCategory.scroll(s, x, y)
            }
            else -> {
                if (searchBar.scrolled(s, x, y)) return
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val mx = mouseX / scale
        val my = mouseY / scale
        for (category in visibleCategoryRenderers) {
            if (category.hoveredTime != null) {
                if (searchBar.textInput.isEmpty()) {
                    openedCategory = category
                    searchCategory = false
                }
                else {
                    openedCategory = if (searchCategory) category else SearchCategoryRenderer
                    searchCategory = !searchCategory
                }
                break
            }
        }
        openedCategory.mouseClicked(mx, my, mouseButton)
        searchBar.mouseClicked(mx, my, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (searchBar.mouseReleased() != InteractionFeedback.NONE) return
        openedCategory.mouseReleased(mouseX / scale, mouseY / scale, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (searchBar.keyTyped(typedChar, keyCode)) {
            InteractionFeedback.OPENED -> {
                openedCategory.closeSettings()
                return
            }
            InteractionFeedback.CLOSED -> return
            else -> {}
        }
        if (openedCategory.keyTyped(typedChar, keyCode) != InteractionFeedback.NONE) return
        super.keyTyped(typedChar, keyCode)
    }

    override fun initGui() {
        for (category in visibleCategoryRenderers) {
            category.onInit()
        }
        SearchCategoryRenderer.onInit()

        if (searchBar.textInput.isEmpty()) {
            if (openedCategory.category.visibility == Visibility.VISIBLE || CategoryGeneral.devMode) return
            else openedCategory = visibleCategoryRenderers.first()
        }
    }

    override fun onGuiClosed() {}

    override fun onResize(mcIn: Minecraft?, w: Int, h: Int) {
        super.onResize(mcIn, w, h)
        if (mcIn != null) {
            val scaledResolution = ScaledResolution(mc)
            scale = scaledResolution.scaledWidth_double / 1600.0
            scaledHeight = scaledResolution.scaledHeight_double / scale

            CategoryTab.scroll = CategoryTab.scroll // Make sure it is in range
        }

        openedCategory.onResize()
    }

    fun onConfigReload() {
        for (category in categoryRenderers) {
            category.onConfigReload()
        }
    }

    override fun doesGuiPauseGame() = false

    fun inArea(mouseX: Double = scaledMouseX(), mouseY: Double = scaledMouseY()): Int {
        if (mouseX in 140.0..400.0 && mouseY in 120.0..(scaledHeight - 120.0)) return 1
        else if (mouseX in 340.0..1500.0 && mouseY in 140.0..760.0) return 2
        return 0
    }

    fun scaledMouseX() = Mouse.getX() * width / mc.displayWidth / scale
    fun scaledMouseY() = (height - Mouse.getY() * height / mc.displayHeight - 1) / scale

    fun setHoveredText(vararg text: String) {
        hoverText = text.toList().flatMap { it.split("\n ") }
    }

    object CategoryTab {
        private const val SIDES_OFFSET = 15
        private const val SCROLL_AMOUNT = 25
        private const val BUTTON_HEIGHT = 40
        private const val BUTTON_OFFSET = 10
        private const val SPACED_BUTTON_HEIGHT = BUTTON_HEIGHT + BUTTON_OFFSET
        private const val CATEGORY_NAME_SCALE = 2f

        private val minScroll
            get() = (((scaledHeight - 240 - SIDES_OFFSET * 2.0) - (visibleCategoryRenderers.size * SPACED_BUTTON_HEIGHT - BUTTON_OFFSET)) / SCROLL_AMOUNT) // Space - ButtonSpace / ScrollAmount
                .coerceAtMost(0.0)
        var scroll: Double = 0.0
            set(value) {
                field = value.coerceIn(minScroll, 0.0)
            }

        fun drawCategoryList() {
            GlStateManager.pushMatrix()
            GUIRenderUtils.renderRoundedRect(140.0, 120.0, 260.0, scaledHeight - 240.0, theme.categoryListBackgroundColor(), 15.0)
            GUIRenderUtils.setUpScissor(140, 120 + SIDES_OFFSET - 1, 260, scaledHeight.toInt() - 240 - SIDES_OFFSET * 2 + 2, scale) // -1 +1 y for rounded corners
            GlStateManager.translate(140.0, 120.0, 0.0)
            val mouseX = (scaledMouseX() - 140.0).roundToInt()
            val mouseY = scaledMouseY() - 120.0
            val withinParams = mouseX in SIDES_OFFSET..(260 - SIDES_OFFSET * 2) && mouseY in SIDES_OFFSET.toDouble()..(scaledHeight - 120 - 2 * SIDES_OFFSET)

            val mScroll = minScroll // scroll is negative
            val maxBarHeight = scaledHeight - 240.0 - SIDES_OFFSET * 2.0
            val barHeight =
                if (mScroll < 0) (maxBarHeight * (visibleCategoryRenderers.size + (mScroll / SPACED_BUTTON_HEIGHT * SCROLL_AMOUNT)) / visibleCategoryRenderers.size) // (Total - Amount of categories that exceed available space) / Total
                else maxBarHeight // max Height * percentage visible
            val sbX = 260.0 - 7.0 - 3.0
            val sbY = SIDES_OFFSET + if (mScroll < 0) (maxBarHeight - barHeight) * (scroll / mScroll) else 0.0
            GUIRenderUtils.renderRect(sbX, sbY, 5.0, barHeight, theme.categorySelectorScrollbarColor())
            //if (mouseX.toDouble() in sbX..sbX + 5.0 && mouseY in sbY..sbY + barHeight) { }  TODO: Scrolling by dragging scrollbar

            var y = SIDES_OFFSET + SCROLL_AMOUNT * scroll

            for (category in visibleCategoryRenderers) {
                if (y >= SIDES_OFFSET - BUTTON_HEIGHT) {
                    category.hoveredTime = if (withinParams && mouseY.toFloat() in y..y + 40f) {
                        if (category.category.description.isNotEmpty()) setHoveredText(category.category.description)
                        category.hoveredTime ?: System.currentTimeMillis()
                    } else null
                    GUIRenderUtils.renderRoundedRect(
                        SIDES_OFFSET.toDouble(), y, 260.0 - SIDES_OFFSET * 2.0, BUTTON_HEIGHT.toDouble(),
                        if (category.hoveredTime != null) GUIRenderUtils.colorGradient(
                            theme.categorySelectorColor(),
                            theme.categorySelectorHoveredColor(),
                            System.currentTimeMillis() - category.hoveredTime!!,
                            500L
                        )
                        else theme.categorySelectorColor(),
                        10.0
                    )
                    fontRendererObj.renderScaledText(
                        category.category.name + if (category.category.visibility == Visibility.DEV) " [DEV]" else "",
                         SIDES_OFFSET + 10f,
                        y.toFloat() + (BUTTON_HEIGHT - fontRendererObj.FONT_HEIGHT * CATEGORY_NAME_SCALE) / 2f,
                        if (category == openedCategory) theme.categorySelectorHighlightTextColor() else theme.categorySelectorTextColor(),
                        CATEGORY_NAME_SCALE,
                        theme.categorySelectorTextShadow()
                    )
                    /* Centered text:
                    fontRendererObj.renderCentredScaledText(
                        category.category.name + if (category.category.visibility == Visibility.DEV) " [DEV]" else "",
                        130f,
                        y.toFloat() + BUTTON_HEIGHT / 2f,
                        if (category == openedCategory) theme.categorySelectorHighlightTextColor() else theme.categorySelectorTextColor(),
                        CATEGORY_NAME_SCALE,
                        theme.categorySelectorTextShadow()
                    )
                     */
                }
                y += SPACED_BUTTON_HEIGHT
                if (y > scaledHeight - 240) break
            }
            GUIRenderUtils.endScissor()
            GlStateManager.popMatrix()
        }
    }

    object SearchCategoryRenderer: CategoryRenderer(Category("SEARCH")) {
        override val visibleValues: List<ValueRenderer>
            get() = visibleCategoryRenderers.flatMap { it.visibleValues } // Already filtered for search input
    }

    enum class InteractionFeedback {
        NONE, INTERACTED, CLOSED, OPENED
    }
}