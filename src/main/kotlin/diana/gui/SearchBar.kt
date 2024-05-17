package diana.gui

import diana.gui.GUIRenderUtils.renderScaledText
import org.apache.commons.lang3.CharUtils
import org.lwjgl.input.Keyboard
import kotlin.math.roundToInt
import kotlin.math.sign

/** Too lazy to clear up :( - 2x **/
object SearchBar {
    const val allowScrolling = true
    const val searchAutoFocus = true
    const val borderThickness = 0.0
    const val width = 300.0
    const val height = 40.0
    const val x = 1460.0 - width
    const val y = 40.0
    const val offsetSides = 10.0
    const val cornerRadius = 10.0

    private val textScale
        get() = (height - offsetSides * 2.0) / GUIRenderUtils.fontRenderer.FONT_HEIGHT
    private var isFocused = false

    /**
     * The first value is the "cursor"
     *
     * indexes: 0-1-2-3-4
     *
     * inputStr: A-B-C-D
     */
    private var textMarked = 0..0
        set(value) {
            field = value
            if (allowScrolling) scrollOffset = calculateScrollOffset(value.first)
        }
    private val textMarkedSorted
        get() = minOf(textMarked.first, textMarked.last)..maxOf(textMarked.first, textMarked.last)
    var searchInput = StringBuilder()
        set(value) {
            field = value
            setSearchInputLengths()
        }

    /** size = [searchInput].length + 1 */
    private var charLengths = listOf(0.0)

    /** Shifts the text to the left if > 0 */
    private var scrollOffset = 0.0
    private var dragging = false
    private var lastClickTime = 0L
    private var lastScroll = 0L

    fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        if (isHovered(mouseX, mouseY)) {
            val cPos = cursorPosInField(mouseX)
            //Handle double-clicking
            if (System.currentTimeMillis() - lastClickTime <= 250 && cPos == textMarked.first && cPos == textMarked.last) {
                textMarked = 0..searchInput.length
            } else {
                isFocused = true
                textMarked = cPos..cPos
                lastScroll = 0L
                dragging = true
            }
            lastClickTime = System.currentTimeMillis()
        } else if (isFocused) {
            isFocused = false
        }
        return true
    }

    fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        if (dragging) {
            dragging = false
            return true
        }
        return false
    }

    /** Uses return value for when [searchInput] has changed depending on [searchAutoFocus] or when search bar is opened or exited */
    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (keyCode == Keyboard.KEY_F && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            textMarked = 0..0
            isFocused = true
            return true
        } else if (isFocused) {
            when {
                keyCode == Keyboard.KEY_ESCAPE -> {
                    textMarked = 0..0
                    isFocused = false
                    return true
                }

                keyCode == Keyboard.KEY_A && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) -> {
                    textMarked = 0..searchInput.length
                }

                keyCode == Keyboard.KEY_BACK -> {
                    if (textMarked.first != textMarked.last) {
                        val sortedMark = textMarkedSorted
                        searchInput = StringBuilder(searchInput.removeRange(sortedMark.first..<sortedMark.last))
                        textMarked = sortedMark.first..sortedMark.first
                    } else if (textMarked.first > 0) {
                        searchInput.deleteCharAt(textMarked.first - 1)
                        setSearchInputLengths()
                        textMarked = textMarked.first - 1..<textMarked.last
                    } else return false
                    return searchAutoFocus
                }

                keyCode == Keyboard.KEY_DELETE -> {
                    if (textMarked.first != textMarked.last) {
                        val sortedMark = textMarkedSorted
                        searchInput = StringBuilder(searchInput.removeRange(sortedMark.first..<sortedMark.last))
                        textMarked = sortedMark.first..sortedMark.first
                    } else if (textMarked.first < searchInput.length) {
                        searchInput.deleteCharAt(textMarked.first)
                        setSearchInputLengths()
                        if (allowScrolling) scrollOffset = calculateScrollOffset(textMarked.first)
                    } else return false
                    return searchAutoFocus
                }

                keyCode == Keyboard.KEY_LEFT -> {
                    if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                        textMarked = textMarked.first..(textMarked.last - 1).coerceAtLeast(0)
                    } else {
                        val dec = if (textMarked.first == textMarked.last) 1 else 0
                        val c = (textMarkedSorted.first - dec).coerceAtLeast(0)
                        textMarked = c..c
                    }
                }

                keyCode == Keyboard.KEY_RIGHT -> {
                    if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                        textMarked = textMarked.first..(textMarked.last + 1).coerceAtMost(searchInput.length)
                    } else {
                        val inc = if (textMarked.first == textMarked.last) 1 else 0
                        val c = (textMarkedSorted.last + inc).coerceAtMost(searchInput.length)
                        textMarked = c..c
                    }
                }

                keyCode == Keyboard.KEY_HOME -> {
                    textMarked =
                        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
                            0..textMarkedSorted.last
                        else 0..0
                }

                keyCode == Keyboard.KEY_END -> {
                    textMarked =
                        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
                            textMarkedSorted.first..searchInput.length
                        else searchInput.length..searchInput.length
                }

                keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER -> {
                    return true
                }

                CharUtils.isAsciiPrintable(typedChar) -> {
                    if (textMarked.first != textMarked.last) {
                        val sortedMark = textMarkedSorted
                        searchInput = StringBuilder(
                            searchInput.replaceRange(
                                sortedMark.first,
                                sortedMark.last,
                                typedChar.toString()
                            )
                        )
                        textMarked = sortedMark.first..sortedMark.first
                    } else {
                        if (!allowScrolling) {
                            val newWidth = calculateScrollOffset(
                                textMarked.first + 1,
                                charLengths.plus(
                                    GUIRenderUtils.fontRenderer.getStringWidth(typedChar.toString()) * textScale + charLengths.last()
                                )
                            )
                            if (newWidth >= 0) return false
                        }
                        searchInput.insert(textMarked.first, typedChar)
                        setSearchInputLengths()
                        textMarked = textMarked.first + 1..textMarked.first + 1
                    }
                    return searchAutoFocus
                }
            }
        }
        return false
    }

    fun scrolled(dWheel: Int, mouseX: Double, mouseY: Double): Boolean {
        if (!isHovered(mouseX, mouseY)) return false
        if (textMarked.first != textMarked.last) {
            val sortedMark = textMarkedSorted
            val actual = (-dWheel).coerceIn(-sortedMark.first, searchInput.length - sortedMark.last)

            if (actual == 0) return true
            textMarked = sortedMark.first + actual..sortedMark.last + actual
            if (allowScrolling) scrollOffset =
                calculateScrollOffset(if (actual < 0) textMarked.first else textMarked.last)
        } else {
            textMarked = (textMarked.first - dWheel).coerceIn(0, searchInput.length).let { it..it }
        }
        return true
    }


    fun drawScreen(mouseX: Double, mouseY: Double, partialTicks: Float, scale: Double, theme: Theme) { //Space width 4
        //Render Search Field
        GUIRenderUtils.renderRoundedRect(x, y, width, height, theme.searchBarBackground(), cornerRadius)
        if (borderThickness != 0.0) {
            GUIRenderUtils.renderRect(x, y, width, height, theme.searchBarBackground())
            GUIRenderUtils.renderRectBorder(
                x,
                y,
                width,
                height,
                borderThickness,
                theme.searchBarBorder()
            )
        }

        //Setup Scissors for scrolling
        GUIRenderUtils.setUpScissorAbsolute(
            (x + offsetSides).roundToInt(),
            y.roundToInt(),
            (x + width - offsetSides).roundToInt(),
            (y + height).roundToInt(),
            scale
        )

        if (isFocused) {
            //Render highlight or cursor - Space width between chars = 4
            if (dragging && searchInput.isNotEmpty()) {
                //Set scrollOffset when mouse is out of bounds
                val scrollingLeft = mouseX <= x + offsetSides
                val scrollingRight = mouseX >= x + width - offsetSides
                if (allowScrolling && (scrollingLeft || scrollingRight)) {
                    if (System.currentTimeMillis() - lastScroll >= 200) {
                        textMarked = textMarked.first..textMarked.last - sign(textMarked.last - cursorPosInField(mouseX).toFloat()).toInt()
                        scrollOffset = calculateScrollOffset(if (scrollingLeft) textMarkedSorted.first else textMarkedSorted.last)
                        lastScroll = System.currentTimeMillis()
                    }
                } else {
                    textMarked = textMarked.first..cursorPosInField(mouseX)
                }
            }

            if (textMarked.first != textMarked.last) {
                val sortedMark = textMarkedSorted
                val char1 = charLengths.getOrNull(sortedMark.first)?.toDouble() ?: 0.0
                val char2 = charLengths.getOrNull(sortedMark.last)?.toDouble()

                if (char2 != null) {
                    GUIRenderUtils.renderRect( // Render marker
                        x + offsetSides - scrollOffset + char1 - 1, // Space between chars: 2
                        y + offsetSides,
                        char2 - char1,
                        height - offsetSides * 2,
                        theme.searchBarTextMarkedColor()
                    )
                }
                renderSearchInput(theme)
            }
            else {
                renderSearchInput(theme)
                GUIRenderUtils.renderRect( // Render Cursor
                    x + offsetSides - scrollOffset + (charLengths.getOrNull(textMarked.first) ?: 0.0),
                    y + offsetSides - 2,
                    1.0,
                    height - offsetSides * 2 + 2,
                    theme.searchBarCursorColor()
                )
            }
        }
        else {
            renderSearchInput(theme) // A bit annoying, but the order matters
        }

        GUIRenderUtils.endScissor()
    }

    private fun renderSearchInput(theme: Theme) {
        GUIRenderUtils.fontRenderer.renderScaledText(searchInput.toString(), (x + offsetSides - scrollOffset).toFloat(), (y + offsetSides).toFloat(), theme.searchBarTextColor(), textScale.toFloat())
    }

    /**
     * Returns the index + 1 of the char hovered by the cursor
     */
    private fun cursorPosInField(mouseX: Double): Int {
        if (charLengths.last() == 0.0) return 0

        val relativeMouse = mouseX + scrollOffset - offsetSides * 2.0 - x

        if (relativeMouse >= charLengths.last()) {
            return charLengths.size - 1
        }

        charLengths.forEachIndexed { index, d ->
            if (relativeMouse <= d) return index
        }

        return charLengths.size - 1
    }

    private fun setSearchInputLengths() {
        if (searchInput.isEmpty()) charLengths = listOf(0.0)
        charLengths = listOf(0.0).plus(searchInput.mapIndexed { index, _ ->
            GUIRenderUtils.fontRenderer.getStringWidth(searchInput.substring(0, index + 1)) * textScale
        })
    }

    private fun calculateScrollOffset(index: Int, lengths: List<Double> = charLengths): Double {
        val length = lengths.getOrNull(index) ?: lengths.lastOrNull() ?: return 0.0
        val offset = textScale * 6.0
        val newScroll = length - width + offsetSides * 2.0 + offset // len - space available

        //Gradually decrease the scroll offset when moving towards the left after the cursor exits the field
        return if (newScroll <= scrollOffset) {
            //Once the distance between the new offset and the old one is larger than the input field
            if (scrollOffset - newScroll + offset * 2.0 >= width - offsetSides * 2.0) {
                (lengths.getOrNull(index - 1) ?: 0.0).let {
                    if (it < offset) it
                    else it - offset
                }
            } else scrollOffset
        } else newScroll.coerceIn(0.0, lengths.last() - width + offsetSides * 2.0 + offset)
    }

    private fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX in x..x + width && mouseY in y..y + height
    }
}