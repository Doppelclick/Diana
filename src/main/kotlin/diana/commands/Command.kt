package diana.commands

import diana.Diana
import diana.Diana.Companion.mc
import diana.config.categories.CategoryGeneral
import diana.config.categories.CategoryInquisitor
import diana.config.categories.CategoryWarps
import diana.core.Burrows
import diana.gui.ConfigGui
import diana.handlers.MessageHandler
import diana.handlers.Render
import diana.soopy.WebsiteConnection
import diana.utils.Utils
import diana.utils.Utils.modMessage
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

class Command : CommandBase() {
    private val help = "§3Diana Solver\n§r" +
            " /diana help §7| This message§r\n" +
            " /diana ignore [list, add [player], remove [player]] §7| View / (add / remove players from) your inquis ignore list§r\n" +
            " /diana clear §7| Clear burrows§r\n" +
            " /diana reload §7| Reload config values from file§r\n" +
            " /diana enablewarp [name] §7| Enable a fast travel destination, supports tab completions§r"

    override fun getCommandName(): String = "diana"

    override fun getCommandUsage(p0: ICommandSender): String = "/$commandName"

    override fun getRequiredPermissionLevel(): Int = 0

    override fun addTabCompletionOptions(sender: ICommandSender?, args: Array<out String>, pos: BlockPos?): MutableList<String>? {
        if (args.size == 1) {
            return getListOfStringsMatchingLastWord(args, "help", "ignore", "clear", "reload", "enablewarp")
        } else if (args.size == 2) {
            when (args[0].lowercase()) {
                "beacon" -> return getListOfStringsMatchingLastWord(args, "help", "block", "beam", "text")
                "ignore" -> return getListOfStringsMatchingLastWord(args, "list", "add", "remove")
                "enablewarp" -> return getListOfStringsMatchingLastWord(args, Diana.warps.filter { !it.enabled.invoke() }.map { it.name })
            }
        } else if (args.size >= 3 && args[0].equals("ignore", true)) {
            if (args[1].equals("add", true)) {
                return getListOfStringsMatchingLastWord(args, MessageHandler.receivedInquisFrom)
            } else if (args[1].equals("remove", true)) {
                return getListOfStringsMatchingLastWord(args, CategoryInquisitor.getIgnoreList())
            }
        }
        return super.addTabCompletionOptions(sender, args, pos)
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        when (args.getOrNull(0)) {
            null -> {
                Thread { mc.addScheduledTask { mc.displayGuiScreen(ConfigGui) } }.start()
            }
            "ignore" -> {
                if (args.size == 1) {
                    modMessage("Ignore List: " + CategoryInquisitor.getIgnoreList())
                } else if (args.size == 2) {
                    if (args[1].equals("list", true) || args[1].equals("view", true)) {
                        modMessage("Ignore List: " + CategoryInquisitor.getIgnoreList())
                    } else {
                        modMessage("§cInvalid args!§r /diana ignore [list, add [player], remove [player]]")
                    }
                } else {
                    when (args[1].lowercase()) {
                        "list", "view" -> {
                            modMessage("Ignore List: ${CategoryInquisitor.getIgnoreList()}")
                        }

                        "add" -> {
                            CategoryInquisitor.addToIgnoreList(args.drop(2).map { it.lowercase() })
                        }

                        "remove" -> {
                            CategoryInquisitor.removeFromIgnoreList(args.drop(2).map { it.lowercase() })
                        }

                        else -> {
                            modMessage("§cInvalid args!§r /diana ignore [list, add [player], remove [player]]")
                        }
                    }
                }
            }

            "clear" -> {
                Burrows.waypoints.clear()
                modMessage("Cleared Waypoints.")
            }

            "clearall" -> {
                Burrows.waypoints.clear()
                Burrows.foundBurrows.clear()
                modMessage("Cleared Waypoints and found burrows.")
            }

            "reload" -> {
                Diana.configSystem.load()
                modMessage("Reloaded config.")
            }

            "enablewarp" -> {
                if (CategoryWarps.setWarp(args.getOrNull(1) ?: return, true)) {
                    modMessage("Enabled ${args[1]} warp.")
                } else {
                    modMessage("§cWarp not found. Try using tab completions!")
                }
            }

            "dev" -> {
                when (args.getOrNull(1)?.lowercase()) {
                    "line1" -> {
                        if (args.getOrNull(2) == "null") Render.line1 = null
                        else {
                            val one = args.getOrNull(2)?.toIntOrNull() ?: return
                            val two = args.getOrNull(3)?.toIntOrNull() ?: return
                            val d = args.getOrNull(4)?.toIntOrNull() ?: 1
                            Render.line1 = Triple(one, two, d)
                        }
                    }
                    "line2" -> {
                        if (args.getOrNull(2) == "null") Render.line2 = null
                        else {
                            val one = args.getOrNull(2)?.toIntOrNull() ?: return
                            val two = args.getOrNull(3)?.toIntOrNull() ?: return
                            val d = args.getOrNull(4)?.toIntOrNull() ?: 1
                            Render.line2 = Triple(one, two, d)
                        }
                    }
                    "arrowlength" -> {
                        if (args.getOrNull(2) == "null") Render.arrowLength = null
                        else {
                            Render.arrowLength = args.getOrNull(2)?.toIntOrNull() ?: 1
                        }
                    }
                    "closest" -> {
                        val pos = mc.objectMouseOver?.blockPos?.let { Vec3(it.x.toDouble() + 0.5, 0.0, it.z.toDouble() + 0.5) } ?: return
                        var pair1: Pair<Int, Int>? = null
                        var pair2: Pair<Int, Int>? = null
                        var distance = 2000.0
                        if (args.getOrNull(2) == "arrow") {
                            if (Burrows.arrowStart != null && Burrows.arrowDir != null) {
                                Burrows.particles.forEachIndexed { indexB, base ->
                                    Burrows.particles.forEachIndexed { indexO, other ->
                                        if (indexO > maxOf(2, indexB) && other != base) {
                                            val i = Utils.interceptDirection(base, other.subtract(base), Burrows.arrowStart!!, Burrows.arrowDir!!)
                                            if (i != null) {
                                                val d = pos.distanceTo(i)
                                                if (d < distance) {
                                                    distance = d
                                                    pair1 = Pair(indexB, indexO)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            Burrows.particles.forEach {
                                Burrows.particles.forEachIndexed { indexB, base ->
                                    Burrows.particles.forEachIndexed { indexO, other ->
                                        if (indexO > maxOf(2, indexB) && other != base) {
                                            Burrows.oldParticles.forEachIndexed { indexB2, base2 ->
                                                Burrows.oldParticles.minus(base2).forEachIndexed { indexO2, other2 ->
                                                    if (indexO2 > maxOf(2, indexB2) && other2 != base2) {
                                                        val i = Utils.intercept(base, other, base2, other2)
                                                        if (i != null) {
                                                            val d = pos.distanceTo(i)
                                                            if (d < distance) {
                                                                distance = d
                                                                pair1 = Pair(indexB, indexO)
                                                                pair2 = Pair(indexB2, indexO2)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        modMessage("${pair1?.toString()} - ${pair2?.toString()} = $distance")
                    }
                    "test" -> {
                        if (args.size == 5) {
                            val x: Int = args[2].toIntOrNull() ?: return
                            val y: Int = args[3].toIntOrNull() ?: return
                            val z: Int = args[4].toIntOrNull() ?: return
                            Burrows.burrow = Vec3(
                                x.toDouble(),
                                y.toDouble(),
                                z.toDouble()
                            ).apply { modMessage("Set burrow to: $this") }
                        }
                    }

                    "playsound" -> {
                        if (args.size == 5) {
                            Utils.playSound(
                                args[2],
                                args[3].toFloatOrNull() ?: return,
                                args[4].toFloatOrNull() ?: return
                            )
                        }
                    }

                    "listparty" -> {
                        modMessage("Party: ${MessageHandler.inParty}: ${MessageHandler.partyMembers}")
                    }

                    "printdata" -> {
                        modMessage("${Burrows.guessPos} \n${Burrows.interceptPos}")
                    }

                    "restartserver" -> {
                        if (WebsiteConnection.connected) {
                            modMessage("Reconnecting...")
                            WebsiteConnection.disconnect()
                            Utils.startTimerTask(5000) {
                                WebsiteConnection.connect()
                                modMessage("-> Connecting")
                            }
                        } else {
                            modMessage("Server not running")
                        }
                    }

                    null -> {
                        CategoryGeneral.devMode =! CategoryGeneral.devMode
                        modMessage("Toggled dev mode: ${CategoryGeneral.devMode}")
                    }

                    else -> {
                        modMessage("§cInvalid args!")
                    }
                }
            }

            else -> {
                modMessage(help)
            }
        }
    }
}