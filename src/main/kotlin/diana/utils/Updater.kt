package diana.utils

import diana.Diana
import com.google.gson.JsonParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

class Updater {
    private val releaseURL = "https://github.com/Doppelclick/Diana/releases/latest"
    private val apiURL = "https://api.github.com/repos/Doppelclick/Diana/releases/latest"
    private var lastCheck: Long = 0L

    fun check() {
        if (System.currentTimeMillis() + 60000 < lastCheck) return
        lastCheck = System.currentTimeMillis()
        Diana.scope.launch {
            while (Diana.mc.thePlayer == null) {
                delay(100)
            }
            delay(1000)
            try {
                val request = URL(apiURL).openConnection()
                request.connect()
                val json = JsonParser()
                val latestRelease = json.parse(InputStreamReader(request.getContent() as InputStream)).getAsJsonObject()
                val latestTag = latestRelease["tag_name"].asString
                val currentVersion = DefaultArtifactVersion(Diana.version)
                val latestVersion = DefaultArtifactVersion(latestTag.substring(1))
                if (currentVersion < latestVersion) {
                    println("Update available")
                    val update = ChatComponentText("§l§2  [UPDATE]  ")
                    update.setChatStyle(
                        update.getChatStyle()
                            .setChatClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, releaseURL))
                            .setChatHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                                        "github - $latestVersion"
                                    )
                                )
                            )
                    )
                    Utils.modMessage(ChatComponentText(Diana.chatTitle + "§cSolver is outdated. Please update to " + latestTag + ".\n").apply {
                        this.appendSibling(
                            update
                        )
                    })
                } else println("No update found")
            } catch (e: Exception) {
                println("An error has occurred connecting to github")
                e.printStackTrace()
            }
        }
    }
}