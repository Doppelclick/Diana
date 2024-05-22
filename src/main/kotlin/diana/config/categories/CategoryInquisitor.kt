package diana.config.categories

import diana.config.*
import diana.core.Burrows
import diana.core.Waypoint
import diana.soopy.WebsiteConnection
import diana.utils.Utils

object CategoryInquisitor : Category("Inquisitor") {
    var inqMode: MultiChooseList<InqModeChoice> by multiChoice("Inquisitor Waypoint Mode", MultiChooseList(InqModeChoice.CHAT), InqModeChoice.entries.toMultiChooseList())
        .listen {
            if (CategoryGeneral.modToggled && (sendMode != ChatChoice.NONE || receiveMode != ChatChoice.NONE)) {
                if (it.contains(InqModeChoice.SOOPY)) WebsiteConnection.connect(true)
                else WebsiteConnection.disconnect()
            }
            return@listen it
        }.apply { description = "How to handle Inquisitor waypoints (send & receive)." }

    var sendMode: ChatChoice by choice("Send to", ChatChoice.PARTY, ChatChoice.entries.toTypedArray())
        .listen {
            if (CategoryGeneral.modToggled && inqMode.contains(InqModeChoice.SOOPY)) {
                if (it == ChatChoice.NONE) {
                    if (receiveMode == ChatChoice.NONE) WebsiteConnection.disconnect()
                }
                else WebsiteConnection.connect(true)
            }
            return@listen it
        }.apply { description = "Where to send coordinates if an Inquisitor is found." }

    var receiveMode: ChatChoice by choice("Receive from", ChatChoice.ALL, ChatChoice.entries.toTypedArray())
        .listen {
            if (CategoryGeneral.modToggled && inqMode.contains(InqModeChoice.SOOPY)) {
                if (it == ChatChoice.NONE) {
                    if (sendMode == ChatChoice.NONE) WebsiteConnection.disconnect()
                }
                else WebsiteConnection.connect(true)
            }
            return@listen it
        }.apply { description = "From who to receive inquisitor waypoints." }

    var allowPatcher by boolean("Patcher Coords", false).apply { description = "When someone uses /patcher sendcoords\n it will act the same as a Diana inquis waypoint." }
    var inqWaypointTimeout by int("Waypoint Timeout", 30000, 0..120000).dev()
    var ignoredPlayers by text("Ignored Players", "").hidden()

    enum class InqModeChoice(override val choiceName: String, override val description: String? = null): NamedChoice {
        SOOPY("Soopy"), CHAT("Chat")
    }
    enum class ChatChoice(override val choiceName: String, override val description: String? = null): NamedChoice {
        NONE("None"), PARTY("Party Chat"), ALL("All Chat")
    }

    fun soopyServerOn(): Boolean = inqMode.contains(InqModeChoice.SOOPY) && (sendMode != ChatChoice.NONE || receiveMode != ChatChoice.NONE)

    fun getIgnoreList(): List<String> = ignoredPlayers.takeIf { it.isNotEmpty() }?.split(", ") ?: emptyList()
    fun addToIgnoreList(players: List<String>) {
        ignoredPlayers = getIgnoreList().plus(players.filter { !getIgnoreList().contains(it) }.apply {
            Utils.modMessage("Added ${this.joinToString()} to your ignore List.")
            Burrows.waypoints.removeIf { it is Waypoint.InquisWaypoint && this.contains(it.player) }
        }).joinToString()
    }
    fun removeFromIgnoreList(players: List<String>) {
        ignoredPlayers = getIgnoreList().apply {
            Utils.modMessage("Removed ${players.intersect(this.toSet()).joinToString()} from your ignore List.")
        }.minus(players.toSet()).joinToString()
    }
}