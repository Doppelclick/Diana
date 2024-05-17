package diana.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import diana.Diana
import diana.config.categories.*
import diana.config.json.CategorySerializer
import diana.config.json.EnumChoiceSerializer
import diana.config.json.ExcludeStrategy
import java.io.File
import java.io.Reader
import java.io.Writer

/**
 * The following code was taken and modified from CCBlueX - LiquidBounce under the GNU General Public License 3.0
 */

class ConfigSystem {
    private val configFile = File("${Diana.mc.mcDataDir}/config/Diana.cfg")
    private val categoryType = TypeToken.get(Category::class.java).type
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .addSerializationExclusionStrategy(ExcludeStrategy())
        .registerTypeHierarchyAdapter(NamedChoice::class.javaObjectType, EnumChoiceSerializer)
        .registerTypeHierarchyAdapter(Category::class.javaObjectType, CategorySerializer)
        .create()
    val categories: ArrayList<Category> = arrayListOf(
        CategoryGeneral,
        CategoryInquisitor,
        CategorySelector,
        CategoryWarps,
        CategoryRender,
        CategoryDebug
    )

    fun load() {
        configFile.runCatching {
            if (!exists()) {
                storeAll()
                return@runCatching
            }

            println("Reading config...")
            deserializeCategories(reader())
            println("Successfully loaded config.")
        }.onFailure {
            println("Unable to load config $it")
            storeAll()
        }
    }

    fun storeAll() {
        configFile.runCatching {
            if (!exists()) {
                println("Created new file (status: ${createNewFile()})")
            }

            println("Writing config...")
            serializeCategory(categories.filter { !it.doNotInclude }, bufferedWriter())
            println("Successfully saved config.")
        }.onFailure {
            println("Unable to store config $it")
        }
    }

    private fun serializeCategory(categories: List<Category>, writer: Writer, gson: Gson = this.gson) {
        writer.use { it.write(gson.toJson(categories.map { gson.toJson(it, categoryType) })) }
    }

    private fun deserializeCategories(reader: Reader, gson: Gson = this.gson) {
        gson.fromJson(reader, JsonElement::class.java)?.let {
            deserializeCategories(categories.filter { !it.doNotInclude }, it)
        }
    }

    private fun deserializeCategories(categories: List<Category>, jsonElement: JsonElement) {
        runCatching {
            for (j in jsonElement.asJsonArray) {
                val jsonObject = j.asJsonObject

                categories.find { it.name == jsonObject.getAsJsonPrimitive("name").asString }?.let { category ->
                    val values = jsonObject.getAsJsonArray("value").map { it.asJsonObject }.associateBy { it["name"].asString!! }

                    for (value in category.value.filter { !it.doNotInclude }) {
                        val currentElement = values[value.name] ?: continue

                        runCatching {
                            value.deserializeFrom(gson, currentElement["value"])
                        }.onFailure {
                            println("Unable to deserialize value ${value.name} $it")
                        }
                    }
                }
            }
        }.onFailure {
            println("Unable to deserialize config $it")
        }
    }
}