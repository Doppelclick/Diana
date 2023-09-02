package diana.commands

import diana.Diana
import diana.config.Config
import diana.core.Burrows
import diana.handlers.MessageHandler
import diana.utils.Utils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

class Command : CommandBase() {
    private val help = "§3Diana Solver\n§r" +
            " /diana help §7| This message§r\n" +
            " /diana ignore [list, add [player], remove [player]] §7| View / (add / remove players from) your inquis ignore list§r\n" +
            " /diana clear §7| Clear burrows§r\n" +
            " /diana reload §7| Reload config values from file§r"

    override fun getCommandName(): String = "diana"

    override fun getCommandUsage(p0: ICommandSender): String = "/$commandName"

    override fun getRequiredPermissionLevel(): Int = 0

    override fun addTabCompletionOptions(sender: ICommandSender?, args: Array<out String>, pos: BlockPos?): MutableList<String>? {
        if (args.size == 1) {
            return getListOfStringsMatchingLastWord(args, "help", "ignore", "clear", "reload")
        } else if (args.size == 2) {
            if (args[0].equals("beacon", true)) {
                return getListOfStringsMatchingLastWord(args, "help", "block", "beam", "text")
            } else if (args[0].equals("ignore", true)) {
                return getListOfStringsMatchingLastWord(args, "list", "add", "remove")
            }
        } else if (args.size >= 3 && args[0].equals("ignore", true)) {
            if (args[1].equals("add", true)) {
                return getListOfStringsMatchingLastWord(args, MessageHandler.receivedInquisFrom)
            } else if (args[1].equals("remove", true)) {
                return getListOfStringsMatchingLastWord(args, Config.getIgnoreList())
            }
        }
        return super.addTabCompletionOptions(sender, args, pos)
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        when (args.getOrNull(0)) {
            null -> {
                Thread { Diana.mc.addScheduledTask { Diana.mc.displayGuiScreen(Config.gui()) } }.start()
            }
            "ignore" -> {
                if (args.size == 1) {
                    Utils.modMessage("Ignore List: " + Config.getIgnoreList())
                } else if (args.size == 2) {
                    if (args[1].equals("list", true) || args[1].equals("view", true)) {
                        Utils.modMessage("Ignore List: " + Config.getIgnoreList())
                    } else {
                        Utils.modMessage("§cInvalid args!§r /diana ignore [list, add [player], remove [player]]")
                    }
                } else {
                    when (args[1].lowercase()) {
                        "list", "view" -> {
                            Utils.modMessage("Ignore List: ${Config.getIgnoreList()}")
                        }

                        "add" -> {
                            Config.addToIgnoreList(args.drop(2).map { it.lowercase() })
                        }

                        "remove" -> {
                            Config.removeFromIgnoreList(args.drop(2).map { it.lowercase() })
                        }

                        else -> {
                            Utils.modMessage("§cInvalid args!§r /diana ignore [list, add [player], remove [player]]")
                        }
                    }
                }
            }

            "clear" -> {
                Burrows.waypoints.clear()
                Utils.modMessage("Cleared Waypoints.")
            }

            "clearall" -> {
                Burrows.waypoints.clear()
                Burrows.foundBurrows.clear()
                Utils.modMessage("Cleared Waypoints and found burrows.")
            }

            "reload" -> {
                Config.loadData()
                Utils.modMessage("Reloaded config.")
            }

            "enablewarp" -> {
                Config.setWarp(args.getOrNull(1) ?: return, true)
                Utils.modMessage("Enabled ${args[1]} warp.")
            }

            "dev" -> {
                when (args.getOrNull(1)?.lowercase()) {
                    "test" -> {
                        if (args.size == 5) {
                            val x: Int = args[2].toIntOrNull() ?: return
                            val y: Int = args[3].toIntOrNull() ?: return
                            val z: Int = args[4].toIntOrNull() ?: return
                            Burrows.burrow = Vec3(
                                x.toDouble(),
                                y.toDouble(),
                                z.toDouble()
                            ).apply { Utils.modMessage("Set burrow to: $this") }
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
                        Utils.modMessage("Party: ${MessageHandler.inParty}: ${MessageHandler.partyMembers}")
                    }

                    "printdata" -> {
                        Utils.modMessage("Arrow: ${Burrows.arrowStart} ${Burrows.arrowDir}\n" +
                                "Particles: ${Burrows.particles}\n" +
                                "Old Particles: ${Burrows.oldParticles}\n" +
                                "Pitch: ${Burrows.sounds}")
                    }

                    else -> {
                        Utils.modMessage("§cInvalid args!")
                    }
                }
            }

            else -> {
                Utils.modMessage(help)
            }
        }
    }
}