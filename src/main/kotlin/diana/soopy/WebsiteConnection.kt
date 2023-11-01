package diana.soopy

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import diana.Diana.Companion.config
import diana.Diana.Companion.scope
import diana.utils.Utils
import diana.utils.getJsonObject
import diana.utils.getJsonPrimitive
import diana.utils.toJsonObject
import java.io.*
import java.net.Socket
import kotlinx.coroutines.*
import java.net.URL
import diana.Diana.Companion.mc as mc

object WebsiteConnection {
    private var socket: Socket? = null
    var connected = false
    private var output: OutputStream? = null
    private var writer: PrintWriter? = null
    private var reconDelay = 1000f

    private var connectedFull = false

    private var gameRunning = true

    private var handlers: ArrayList<EventHandler> = arrayListOf()

    private var streams: ArrayList<Closeable> = arrayListOf()

    private val sendDataArr = mutableListOf<String>()

    private val json = JsonParser()
    val socketData: JsonObject = try {
        val request = URL("https://soopy.dev/socketserver/data.json").openConnection()
        request.connect()
        json.parse(InputStreamReader(request.getContent() as InputStream).readText()).getAsJsonObject()
    } catch (e: Exception) {
        println("Error reading Soopy SocketData")
        e.printStackTrace()
        JsonObject()
    }

    fun onInit() {
        handlers = arrayListOf(SoopyApisServer, SoopyV2Server)
        connect()

        scope.launch {
            while (gameRunning) {
                if (connected && socket != null) {
                    if (sendDataArr.isNotEmpty()) {
                        sendDataArr.forEach {
                            writer?.println(it)
                        }
                        sendDataArr.clear()
                    } else {
                        delay(100)
                    }
                } else {
                    delay(1000)
                }
            }
        }
    }

    fun onGameUnload() {
        gameRunning = false
        disconnect()
    }

    fun connect(preConfig: Boolean = false, preConfigToggle: Boolean = false) {
        if ((!config.toggle &&! preConfigToggle) || !gameRunning || connected || !socketData.has("port") || (!preConfig && (config.inqWaypointMode == 0 || (config.receiveInq == 0 && config.sendInq == 0)))) return

        connectedFull = false
        println("Connecting to Soopy socket")

        try {
            socket = Socket("soopy.dev", socketData.getJsonPrimitive("port")?.asInt ?: 9898)
        } catch (e: IOException) {
            println("Socket error: ${e.message}")
            println("Reconnecting in $reconDelay ms")
            Utils.startTimerTask(reconDelay.toLong()) { connect() }
            reconDelay *= 1.5f
            return
        }
        output = socket!!.getOutputStream()
        writer = PrintWriter(output!!, true)
        connected = true
        reconDelay = 1000f

        scope.launch {
            val input = socket?.getInputStream()
            val reader = BufferedReader(InputStreamReader(input!!))
            streams.add(input)
            streams.add(reader)

            var shouldCont = true

            while (connected && socket != null && shouldCont && gameRunning) {
                try {
                    reader.readLine()?.let { data ->
                        json.parse(data).toJsonObject()?.run { onData(this) }
                    }
                } catch (e: IOException) {
                    if (!connected) return@launch
                    println("SOCKET ERROR (soopyApis/websiteConnection.js)")
                    println(e.message)
                    disconnect()
                    delay(5000)
                    println("Attempting to reconnect to the server")
                    shouldCont = false
                    connect()
                }
            }

            val shouldReCon = connected && shouldCont
            if (shouldReCon) {
                delay(1000)
                println("Attempting to reconnect to the server")
                connect()
            }
        }
        println("Connection successful")
    }

    fun disconnect() {
        if (!connected || socket != null) {
            println("Disconnecting from soopy socket")
            connected = false
            writer?.close()
            output?.close()
            streams.forEach { it.close() }
            streams.clear()
            socket?.close()
            socket = null
        }
    }

    fun sendData(data: String) {
        if (!connected || socket == null) return
        sendDataArr.add(data.replace("\n", ""))
    }

    private fun onData(data: JsonObject) {
        when (data.getJsonPrimitive("type")?.asString ?: "") {
            socketData.getJsonObject("packetTypesReverse")?.getJsonPrimitive("connectionSuccess")?.asString -> {
                var serverId = java.util.UUID.randomUUID().toString().replace("-", "")
                try {
                    mc.sessionService.joinServer(
                        mc.session.profile,
                        mc.session.token,
                        serverId
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    serverId = ""
                }
                if (serverId.isEmpty()) {
                    disconnect()
                    println("Failed to establish a full connection")
                    return
                }
                sendData(createPacket(
                        socketData.getJsonObject("packetTypesReverse")!!.getJsonPrimitive("connectionSuccess")!!.asString,
                        "0",
                        JsonObject().apply {
                            addProperty("username", mc.session.profile.name) //mc.thePlayer could still be null
                            addProperty("uuid", mc.session.profile.id.toString())
                            addProperty("serverId", serverId)
                        }
                ))
                handlers.forEach { handler -> handler.onConnect() }
                connectedFull = true
                println("Full connection successful. Handlers (${handlers.size}): ${handlers.map { "ID: ${it.appId}" }}")
            }
            socketData.getJsonObject("packetTypesReverse")?.getJsonPrimitive("data")?.asString -> {
                data.getJsonObject("data")?.let { receivedData ->
                    handlers.find { it.appId == data.getJsonPrimitive("server")?.asString }?.onData(receivedData)
                } ?: println(data.getJsonPrimitive("noHandlerMessage")?.asString ?: "noHandlerMessage not parsed")
            }
            socketData.getJsonObject("packetTypesReverse")?.getJsonPrimitive("serverReboot")?.asString -> {
                disconnect()
                Utils.startTimerTask(5000) { connect() }
                println("Soopy Server rebooting. Rejoining in 5 seconds")
            }
            socketData.getJsonObject("packetTypesReverse")?.getJsonPrimitive("ping")?.asString -> {
                sendData(
                    createPacket(
                        socketData.getJsonObject("packetTypesReverse")!!.getJsonPrimitive("ping")!!.asString,
                        "0"
                    )
                )
            }
        }
    }

    fun createDataPacket(data: JsonObject, server: String = socketData.getJsonObject("serverNameToId")?.getJsonPrimitive("soopyapis")?.asString ?: ""): String {
        return createPacket(socketData.getJsonObject("packetTypesReverse")!!.getJsonPrimitive("data")!!.asString, server, data)
    }

    fun createPacket(type: String, server: String = socketData.getJsonObject("serverNameToId")?.getJsonPrimitive("soopyapis")?.asString ?: "", data: JsonElement = JsonObject()): String {
        return JsonObject().apply {
            addProperty("type", type)
            addProperty("server", server) //THESE HAVE TO BE STRINGS!!!
            add("data", data)
        }.toString()
    }
}

object SoopyApisServer: WebsiteCommunicator(WebsiteConnection.socketData.getJsonObject("serverNameToId")?.getJsonPrimitive("soopyapis")?.asString ?: "0") {
    override val modVersion = "0.1.17"

    override fun onDataCallback(data: JsonObject) {
        when (data.getJsonPrimitive("type")?.asString) {
            "message" -> {
                Utils.modMessage(data.getJsonPrimitive("message")?.asString ?: return)
            }
        }
    }
}

interface EventHandler {
    val appId: String
    fun onConnect()
    fun onData(data: JsonObject)
}