package diana.gui.values

import diana.config.RangedValue
import kotlin.math.roundToInt

class IntRenderer(
    override val value: RangedValue<Int>
) : FloatRenderer(value) {
    override fun setValue(v: Float) {
        value.set(v.roundToInt())
    }

    override fun getValue(): Float {
        return value.value.toFloat()
    }

    override fun getValueString(): String {
        return value.value.toString()
    }
}