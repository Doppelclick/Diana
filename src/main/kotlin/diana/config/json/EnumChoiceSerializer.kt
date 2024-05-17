package diana.config.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import diana.config.NamedChoice
import java.lang.reflect.Type

object EnumChoiceSerializer : JsonSerializer<NamedChoice> {

    override fun serialize(src: NamedChoice, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.choiceName)
    }

}