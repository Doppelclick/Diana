package diana.config

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import diana.config.categories.CategoryGeneral
import diana.config.json.Exclude
import java.awt.Color
import java.util.*
import java.util.function.Predicate
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.reflect.KProperty

/**
 * The following code was taken and modified from CCBlueX - LiquidBounce under the GNU General Public License 3.0
 */

typealias ValueListener<T> = (T) -> T

open class Value<T : Any>(
    @SerializedName("name") open val name: String,
    @SerializedName("value") internal var value: T,
    @Exclude val valueType: ValueType,
    @Exclude internal var visibility: Visibility = Visibility.VISIBLE,
    @Exclude internal val default: T = value
) {
    @Exclude
    var doNotInclude = false
    @Exclude
    private val listeners = mutableListOf<ValueListener<T>>()
    @Exclude
    val dependencies = mutableListOf<() -> Boolean>()
    @Exclude
    var description: String = ""
        set(value) {
            field = value
            printableDescription = field.replace("\n ", " ")
        }
    @Exclude /** Description without formatting codes **/
    var printableDescription = ""

    open fun set(t: T): Boolean { // temporary set value
        // Do nothing if value is the same
        if (t == value) return false

        value = t

        // check if value is really accepted
        var currT = t
        runCatching {
            currT = runListeners(currT)
        }.onSuccess {
            value = currT
            return true
        }.onFailure { ex ->
            println("Failed to set ${this.name} from ${this.value} to $t $ex")
        }
        return false
    }

    fun runListeners(t: T) : T {
        var re: T = t
        listeners.forEach {
            re = it(t)
        }
        return re
    }

    operator fun getValue(u: Any, property: KProperty<*>): T = get()

    operator fun setValue(u: Any?, property: KProperty<*>, t: T) {
        set(t)
    }

    fun get() : T = value

    fun reset() = set(default)

    open fun deserializeFrom(gson: Gson, element: JsonElement) {
        set(gson.fromJson(element, value.javaClass))
    }

    fun listen(listener: ValueListener<T>): Value<T> {
        listeners += listener
        return this
    }

    fun hidden(): Value<T> {
        visibility = Visibility.HIDDEN
        return this
    }
    fun dev(): Value<T> {
        visibility = Visibility.DEV
        return this
    }
    fun doNotInclude(): Value<T> {
        doNotInclude = true
        return this
    }

    fun dependsOn(predicate: () -> Boolean): Value<T> {
        dependencies.add(predicate)
        return this
    }

    open fun notHidden(): Boolean {
        return visibility.let { it == Visibility.VISIBLE || it == Visibility.DEV && CategoryGeneral.devMode }
                && dependencies.all { it() }
    }

    @Suppress("UNCHECKED_CAST")
    open fun setByString(string: String) {
        if (this.value is Boolean) {
            val newValue = when (string.lowercase(Locale.ROOT)) {
                "true", "on" -> true
                "false", "off" -> false
                else -> throw IllegalArgumentException()
            }

            set(newValue as T)
        } else if (this.value is Color) {
            if (string.startsWith("#")) set(Color(string.substring(1).toInt(16)) as T)
            else set(Color(string.toInt()) as T)
        } else {
            throw IllegalStateException()
        }
    }
}

enum class ValueType {
    ACTION, BOOLEAN, CATEGORY, FLOAT, INT, TEXT, COLOR, CHOICE, CHOOSE, MULTI_CHOOSE, UNKNOWN
}

enum class Visibility {
    VISIBLE, HIDDEN, BLURRED, DEV
}

class RangedValue<T : Any>(
    name: String, value: T, @Exclude val range: ClosedRange<*>, type: ValueType
) : Value<T>(name, value, type) {

    fun getFrom(): Double {
        return (this.range.start as Number).toDouble()
    }

    fun getTo(): Double {
        return (this.range.endInclusive as Number).toDouble()
    }

    fun getDistance(): Double {
        return abs(getTo() - getFrom())
    }
}


typealias Action = () -> Unit

class ActionValue<T : Action>(
    name: String, value: T
) : Value<T>(name, value, ValueType.ACTION) {
    init {
        doNotInclude()
    }
}


class ChooseListValue<T: NamedChoice>(
    name: String, value: T, @Exclude val choices: Array<T>
) : Value<T>(name, value, ValueType.CHOOSE) {
    override fun deserializeFrom(gson: Gson, element: JsonElement) {
        val name = element.asString

        setByString(name)
    }

    override fun setByString(string: String) {
        this.value = choices.first { it.choiceName == string }
    }
}

class MultiChooseListValue<T: NamedChoice>(
    name: String, value: MultiChooseList<T>, @Exclude val choices: MultiChooseList<T>
) : Value<MultiChooseList<T>>(name, value, ValueType.MULTI_CHOOSE) {
    init {
        value.listListeners = { u -> runListeners(u) }
    }

    override fun set(t: MultiChooseList<T>): Boolean {
        if (super.set(t)) {
            value.listListeners = { u -> runListeners(u) }
            return true
        }
        return false
    }

    override fun deserializeFrom(gson: Gson, element: JsonElement) {
        value.clear()
        element.asJsonArray.mapNotNullTo(value) { c -> choices.find { it.choiceName == c.asString } }
    }

    override fun setByString(string: String) {
        choices.find { it.choiceName == string }?.let {
            set(it)
        }
    }

    fun set(t: T) {
        if (value.contains(t)) value.remove(t)
        else value.add(t)
    }
}

interface NamedChoice {
    val choiceName: String
    val description: String?
}

class MultiChooseList<T> : ArrayList<T> {
    var listListeners: ((MultiChooseList<T>) -> MultiChooseList<T>)? = null

    constructor(): super()
    constructor(collection: Collection<T>) : super(collection)
    constructor(vararg elements: T) : super(elements.toList())

    override fun add(element: T): Boolean {
        val re = super.add(element)
        runCatching { listListeners?.invoke(this) }
        return re
    }

    override fun add(index: Int, element: T) {
        super.add(element)
        runCatching { listListeners?.invoke(this) }
    }

    override fun remove(element: T): Boolean {
        val re = super.remove(element)
        runCatching { listListeners?.invoke(this) }
        return re
    }

    override fun removeAt(index: Int): T {
        val re = super.removeAt(index)
        runCatching { listListeners?.invoke(this) }
        return re
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        val re = super.removeIf(filter)
        runCatching { listListeners?.invoke(this) }
        return re
    }
}

fun <T> Collection<T>.toMultiChooseList(): MultiChooseList<T> {
    return MultiChooseList(this)
}