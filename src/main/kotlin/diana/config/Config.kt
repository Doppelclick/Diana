package diana.config

import diana.Diana
import diana.core.Burrows
import diana.core.Waypoint
import diana.utils.Utils
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.*
import java.awt.Color
import java.io.File

object Config : Vigilant(File("./config/${Diana.modName}.toml"), "Diana", sortingBehavior = Sorting) {
    @Property(
        category = "General",
        name = "Toggle",
        type = PropertyType.SWITCH
    )
    var toggle = true

    @Property(
        category = "General",
        name = "Guess",
        description = "Whether the mod should guess the location of the burrow.",
        type = PropertyType.SWITCH
    )
    var guess = true

    @Property(
        category = "General",
        name = "Intercept",
        description = "Whether the mod should calculate an intercept as the burrow guess.",
        type = PropertyType.SWITCH,
        hidden = true
    )
    var calculateIntercept = true

    @Property(
        category = "General",
        name = "Intercept as full block",
        description = "This will draw the burrow calculated with the intercept on a full block .",
        type = PropertyType.SWITCH,
    )
    var interceptAsFullBlock = false

    @Property(
        category = "General",
        name = "Guess Tolerance",
        description = "Tolerance for intercept calculation to prevent 'flickering'.",
        type = PropertyType.DECIMAL_SLIDER,
        minF = 0f,
        maxF = 100f,
        hidden = true
    )
    var guessTolerance = 5f

    @Property(
        category = "General",
        name = "ignore accuracy checks",
        type = PropertyType.SWITCH,
        hidden = true
    )
    var ignoreAccuracyChecks = false

    @Property(
        category = "General",
        name = "Interpolation",
        description = "Changes of the location of the guess will be drawn smoothly.",
        type = PropertyType.SWITCH
    )
    var interpolation = true

    @Property(
        category = "General",
        name = "Proximity",
        description = "Detects nearby burrows.",
        type = PropertyType.SWITCH
    )
    var proximity = true

    @Property(
        category = "General",
        name = "Messages",
        description = "Sends messages when the burrow location is found, etc.",
        type = PropertyType.SWITCH
    )
    var messages = false

    @Property(
        category = "Inquisitor",
        name = "Send Inquisitor",
        description = "Where to send coordinates if an Inquisitor is found.",
        type = PropertyType.SELECTOR,
        options = ["Off", "Party", "All Chat"]
    )
    var sendInq = 1

    @Property(
        category = "Inquisitor",
        name = "Receive Inquisitor",
        description = "From who to receive inquisitor waypoints.",
        type = PropertyType.SELECTOR,
        options = ["Off", "Party", "All Chat"]

    )
    var receiveInq = 2

    @Property(
        category = "Inquisitor",
        name = "Receive Inquisitor from patcher",
        description = "When someone uses /patcher sendcoords  it will act the same as a Diana inquis waypoint.",
        type = PropertyType.SWITCH

    )
    var receiveInqFromPatcher = false

    @Property(
        category = "Inquisitor",
        name = "Ignored Players",
        type = PropertyType.TEXT,
        hidden = true
    )
    var ignoredPlayers = ""

    fun getIgnoreList(): List<String> = ignoredPlayers.takeIf { it.isNotEmpty() }?.split(", ") ?: emptyList()
    fun addToIgnoreList(players: List<String>) {
        ignoredPlayers = getIgnoreList().plus(players.filter { !getIgnoreList().contains(it) }.apply {
            Utils.modMessage("Added ${this.joinToString()} to your ignore List.")
            Burrows.waypoints.removeIf { it is Waypoint.InquisWaypoint && this.contains(it.player) }
        }).joinToString()
        markDirty()
    }
    fun removeFromIgnoreList(players: List<String>) {
        ignoredPlayers = getIgnoreList().apply {
            Utils.modMessage("Removed ${players.intersect(this.toSet()).joinToString()} from your ignore List.")
        }.minus(players.toSet()).joinToString()
        markDirty()
    }

    @Property(
        category = "Customisation",
        name = "Seperate color for guess burrow",
        description = "If the guess burrow should have its own color.",
        type = PropertyType.SWITCH
    )
    var guessSeparateColor = false

    @Property(
        category = "Customisation",
        name = "Guess burrow color",
        description = "The color of the guess burrow.",
        type = PropertyType.COLOR
    )
    var guessColor = Color.BLUE

    @Property(
        category = "Customisation",
        name = "Start Color",
        description = "The color of the start (empty) burrow.",
        type = PropertyType.COLOR
    )
    var startColor = Color.GREEN

    @Property(
        category = "Customisation",
        name = "Mob Color",
        description = "The color of the mob burrow (usually the second or third in a chain).",
        type = PropertyType.COLOR
    )
    var mobColor = Color.RED

    @Property(
        category = "Customisation",
        name = "Unknown Color",
        description = "The color of an unknown (usually mob/treasure) burrow (typically the third, before the mod has gathered enough info).",
        type = PropertyType.COLOR
    )
    var unknownColor = Color.WHITE

    @Property(
        category = "Customisation",
        name = "Treasure Color",
        description = "The color of a treasure burrow (usually the third or finish in a chain).",
        type = PropertyType.COLOR
    )
    var treasureColor = Color.YELLOW

    @Property(
        category = "Customisation",
        name = "Inquisitor Color",
        description = "The color of inquisitor waypoints.",
        type = PropertyType.COLOR
    )
    var inquisitorColor = Color(226, 167, 60)

    @Property(
        category = "Beacon",
        name = "Block",
        description = "Render the beacon block.",
        type = PropertyType.SWITCH
    )
    var beaconBlock = true

    @Property(
        category = "Beacon",
        name = "Beam",
        description = "Render the beacon beam.",
        type = PropertyType.SWITCH
    )
    var beaconBeam = true

    @Property(
        category = "Beacon",
        name = "Text",
        description = "Render the beacon text.",
        type = PropertyType.SWITCH
    )
    var beaconText = true

    @Property(
        category = "Warps",
        name = "castle",
        type = PropertyType.SWITCH,
        hidden = true
    )
    var castleWarp = true

    @Property(
        category = "Warps",
        name = "crypt",
        type = PropertyType.SWITCH,
        hidden = true
    )
    var cryptWarp = true

    @Property(
        category = "Warps",
        name = "da",
        type = PropertyType.SWITCH,
        hidden = true
    )
    var daWarp = true

    @Property(
        category = "Warps",
        name = "museum",
        type = PropertyType.SWITCH,
        hidden = true
    )
    var museumWarp = true

    @Property(
        category = "Warps",
        name = "wizard",
        type = PropertyType.SWITCH,
        hidden = true
    )
    var wizardWarp = true

    @Property(
        category = "Debug",
        name = "Force hub",
        type = PropertyType.SWITCH,
    )
    var forceHub = false

    fun setWarp(name: String, case: Boolean) {
        javaClass.declaredFields.find { it.name == "${name}Warp" }
            ?.takeIf { it.genericType == Boolean::class.java }?.apply {
                setBoolean(this, case)
                markDirty()
                println("Set ${this.name} warp to $case")
            }
    }

    init {
        initialize()
        addDependency("guessColor", "guessSeparateColor")
    }
    
    private object Sorting: SortingBehavior() {
        override fun getCategoryComparator(): Comparator<in Category> = compareBy { listOf("General", "Inquisitor", "Customisation", "Beacon", "Warps", "Debug").indexOf(it.name) }
    }
}