package diana.soopy

import com.google.gson.JsonObject
import diana.utils.getJsonObject
import diana.utils.getJsonPrimitive

open class WebsiteCommunicator(final override val appId: Int): EventHandler {
    private var connected = false
    open val modVersion: String = "UNKNOWN"

    override fun onConnect() {
        try {
            WebsiteConnection.sendData(
                WebsiteConnection.createPacket(
                    WebsiteConnection.socketData.getJsonObject("packetTypesReverse")?.getJsonPrimitive("joinServer")?.asInt ?: 2,
                    appId,
                    JsonObject().apply {
                        addProperty("version", modVersion)
                    }
                )
            )
            try {
                onConnectCallback()
                connected = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onData(data: JsonObject) {
        if (!data.has("type")) return
        try {
            onDataCallback(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendData(data: JsonObject) {
        val packet = WebsiteConnection.createDataPacket(data, appId)
        WebsiteConnection.sendData(packet)
    }

    open fun onConnectCallback() {

    }

    open fun onDataCallback(data: JsonObject) {

    }
}