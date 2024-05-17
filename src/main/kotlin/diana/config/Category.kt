package diana.config

import java.awt.Color

/**
 * The following code was taken and modified from CCBlueX - LiquidBounce under the GNU General Public License 3.0
 */

open class Category(
    name: String,
    value: MutableList<Value<*>> = mutableListOf(),
    valueType: ValueType = ValueType.CATEGORY
) : Value<MutableList<Value<*>>>(name, value, valueType) {
    val containedValues: Array<Value<*>>
        get() = this.value.toTypedArray()


    private fun <T : Any> value(
        name: String,
        default: T,
        valueType: ValueType = ValueType.UNKNOWN
    ) = Value(name, default, valueType).apply { this@Category.value.add(this) }

    private fun <T : Any> rangedValue(name: String, default: T, range: ClosedRange<*>, valueType: ValueType) =
        RangedValue(name, default, range, valueType).apply { this@Category.value.add(this) }

    protected fun boolean(name: String, default: Boolean) =
        value(name, default, ValueType.BOOLEAN)

    protected fun float(name: String, default: Float, range: ClosedFloatingPointRange<Float>) =
        rangedValue(name, default, range, ValueType.FLOAT)

    protected fun int(name: String, default: Int, range: IntRange) =
        rangedValue(name, default, range, ValueType.INT)

    protected fun text(name: String, default: String) =
        value(name, default, ValueType.TEXT)

    protected fun color(name: String, default: Color) =
        value(name, default, ValueType.COLOR)

    protected fun action(name: String, action: () -> Unit) =
        ActionValue(name, action).apply { this@Category.value.add(this) }

    protected fun <T: NamedChoice> choice(name: String, default: T, choices: Array<T>) =
        ChooseListValue(name, default, choices).apply { this@Category.value.add(this) }

    protected fun <T: NamedChoice> multiChoice(name: String, default: MultiChooseList<T>, choices: MultiChooseList<T>) =
        MultiChooseListValue(name, default, choices).apply { this@Category.value.add(this) }
}
