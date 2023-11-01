package diana.soopy
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import diana.Diana.Companion.config
import diana.Diana.Companion.mc
import diana.handlers.EntityHandler
import diana.handlers.LocationHandler
import diana.utils.Utils
import diana.utils.getJsonArray
import diana.utils.getJsonObject
import diana.utils.getJsonPrimitive
import net.minecraft.util.BlockPos

object SoopyV2Server : WebsiteCommunicator(WebsiteConnection.socketData.getJsonObject("serverNameToId")?.getJsonPrimitive("soopyv2")?.asString ?: "3") {
    override val modVersion = "2.1.202"

    override fun onConnectCallback() {
        if (LocationHandler.lastServer != null && LocationHandler.area != "UNKNOWN" && LocationHandler.location != "UNKNOWN") {
            LocationHandler.lastSentServer = System.currentTimeMillis()
            setServer(LocationHandler.lastServer!!, LocationHandler.area, LocationHandler.location)
        }
    }

    override fun onDataCallback(data: JsonObject) {
        when (data.getJsonPrimitive("type")?.asString) {
            "inquisData" -> {
                if (config.receiveInq != 0 && config.inqWaypointMode != 0) {
                    val location = data.getJsonArray("location")?.takeIf { it.size() == 3 } ?: return
                    val user = data.getJsonPrimitive("user")?.asString ?: return
                    try {
                        val pos = BlockPos(location.get(0).asInt, location.get(1).asInt, location.get(2).asInt)
                        EntityHandler.handleInquisWaypointReceived(pos, user)
                    } catch (e: Exception) {
                        println("Formatting issue in received Soopy Inquisitor Waypoint")
                    }
                }
            }
        }
    }


    fun sendInquisData(position: BlockPos, partyMembers: List<String>) {
        val data = JsonObject().apply {
            add("loc", JsonArray().apply {
                add(JsonPrimitive(position.x))
                add(JsonPrimitive(position.y))
                add(JsonPrimitive(position.z))
            })
            add("pmemb", JsonArray().apply {
                partyMembers.forEach { this.add(JsonPrimitive(it)) }
            })
            addProperty("limitPMemb", partyMembers.isNotEmpty() && config.sendInq != 2)
        }
        sendData(JsonObject().apply {
            addProperty("type", "inquisData")
            add("data", data)
            addProperty("name", mc.thePlayer.displayName.unformattedTextForChat)
        })
    }

    fun setServer(server: String, area: String, areaFine: String) {
        sendData(JsonObject().apply {
            addProperty("type", "server")
            addProperty("server", server)
            addProperty("area", area)
            addProperty("areaFine", areaFine)
        })
        Utils.startTimerTask(300000) {
            if (WebsiteConnection.connected && System.currentTimeMillis() - LocationHandler.lastSentServer >= 300000) {
                setServer(
                    LocationHandler.lastServer!!,
                    LocationHandler.area,
                    LocationHandler.location
                )
            }
        }
    }
}