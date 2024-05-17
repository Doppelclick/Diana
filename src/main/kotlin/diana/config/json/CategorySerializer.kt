package diana.config.json

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import diana.config.Category
import java.lang.reflect.Type

object CategorySerializer : JsonSerializer<Category> {

    override fun serialize(
        src: Category,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val obj = JsonObject()

        obj.addProperty("name", src.name)
        obj.add("value", context.serialize(src.value))

        return obj
    }

}