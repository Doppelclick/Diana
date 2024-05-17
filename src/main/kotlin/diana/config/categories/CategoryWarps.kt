package diana.config.categories

import diana.config.Category
import diana.config.Value
import diana.config.ValueType
import diana.config.Visibility

object CategoryWarps : Category("Warps") {
    var castle by boolean("Castle", true)
    var crypt by boolean("Crypt", true)
    var da by boolean("DA", true)
    var museum by boolean("Museum", true)
    var wizard by boolean("Wizard", true)

    init {
        visibility = Visibility.DEV
    }

    @Suppress("UNCHECKED_CAST")
    fun setWarp(name: String, case: Boolean): Boolean {
        this@CategoryWarps.value.find { it.name == name && it.valueType == ValueType.BOOLEAN }
            ?.apply {
                if ((this as? Value<Boolean>)?.set(case) == true) {
                    println("Set ${this.name} warp to $case")
                    return true
                }
            }
        return false
    }
}