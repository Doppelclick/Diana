package diana.config.categories

import diana.config.Category
import java.awt.Color

object CategoryRender : Category("Render") {
    var interpolation by boolean("Interpolation", true).apply { description = "Changes of the location of the guess will smooth." }
    var guessSeparateColor by boolean("Separate Guess Color", true).apply { description = "If the guess burrow should have its own color." }
    var guessColor by color("Guess Color", Color.BLUE).dependsOn { guessSeparateColor }
    var startColor by color("Start Color", Color.GREEN).apply { description = "The color of the start (empty) burrow." }
    var mobColor by color("Mob Color", Color.RED).apply { description = "The color of the mob burrow (usually the second or third in a chain)." }
    var unknownColor by color("Unknown Color", Color.WHITE).apply { description = "The color of an unknown (usually mob/treasure) burrow\n (typically the third, before the mod has gathered enough info)." }
    var treasureColor by color("Treasure Color", Color.YELLOW).apply { description = "The color of a treasure burrow (usually the third or last in a chain)." }
    var inquisitorColor by color("Inquisitor Color", Color(226, 167, 60)).apply { description = "The color of inquisitor waypoints." }

    var beaconBlock by boolean("Beacon Block", true)
    var beaconBeam by boolean("Beacon Beam", true)
    var beaconText by boolean("Beacon Text", true)
}