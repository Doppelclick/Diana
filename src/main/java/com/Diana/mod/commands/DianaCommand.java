package com.Diana.mod.commands;

import com.Diana.mod.Diana;
import com.Diana.mod.config.config;
import com.Diana.mod.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;

import java.util.*;

public class DianaCommand extends CommandBase {
    static String help() {
        return "§3Diana Solver\n§r"
                + " /diana help §7| This message§r\n"
                + " /diana toggle §7| Toggle the mod§r (" + config.understandMe(Diana.toggle) + ")\n"
                + " /diana guess §7| Toggle burrow guess§r (" + config.understandMe(Diana.guess) + ")\n"
                + " /diana interpolation §7| Toggle interpolation for guess burrow§r (" + config.understandMe(Diana.interpolation) + ")\n"
                + " /diana proximity §7| Toggle nearby burrow detection§r (" + config.understandMe(Diana.proximity) + ")\n"
                + " /diana messages §7| Toggle messages§r (" + config.understandMe(Diana.messages) + ")\n"
                + " /diana send §7| Change who to send inquis coords to§r (" + allOrParty(Diana.sendInqToAll) + ")\n"
                + " /diana receive §7| Change from who to receive inquis coords§r (" + allOrParty(Diana.receiveInqFromAll) + ")\n"
                + " /diana beacon §7[help, block, beam, text]§r\n"
                + " /diana ignore [list, add [player], remove [player]] §7| View / (add / remove players from) your inquis ignore list§r\n"
                + " /diana clear §7| Clear burrows§r\n"
                + " /diana reload §7| Reload config values from file§r";
    }

    static String beaconHelp() {
        return "§3Diana Solver §rbeacon options\n"
                + " /diana beacon help §7| This message§r\n"
                + " /diana beacon block §7| Toggle beacon block§r (" + config.understandMe(Diana.block) + ")\n"
                + " /diana beacon beam §7| Toggle beacon beam§r (" + config.understandMe(Diana.beam) + ")\n"
                + " /diana beacon text §7| Toggle beacon text§r (" + config.understandMe(Diana.text) + ")";
    }

    static String allOrParty(boolean all) {
        return "§r" + (all ? "all" : "§9party") + "§r";
    }

    @Override
    public String getCommandName() {
        return "diana";
    }
    @Override
    public String getCommandUsage(ICommandSender arg0) {
        return "/" + getCommandName();
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] strings, BlockPos pos) {
        if (strings.length == 1) {
            return getListOfStringsMatchingLastWord(strings, "help", "toggle", "guess", "interpolation", "proximity", "messages", "send", "receive", "beacon", "ignore", "clear", "reload");
        } else if ((strings.length == 2)) {
            if (strings[0].equalsIgnoreCase("beacon")) {
                return getListOfStringsMatchingLastWord(strings, "help", "block", "beam", "text");
            } else if (strings[0].equalsIgnoreCase("ignore")) {
                return getListOfStringsMatchingLastWord(strings, "list", "add", "remove");
            }
        } else if (strings.length >= 3 && strings[0].equalsIgnoreCase("ignore") && (strings[1].equalsIgnoreCase("add") || strings[1].equalsIgnoreCase("remove"))) {
            List<String> players = new ArrayList<>(Diana.receivedInquisFrom);
            players.removeAll(Utils.getArgsAfter(strings, 2));
            return getListOfStringsMatchingLastWord(strings, players);
        }
        return null;
    }
    @Override
    public void processCommand(ICommandSender sender, String[] strings) {
        if (strings.length < 1) {
            Utils.sendModMessage(new ChatComponentText(help()));
        } else {
            switch (strings[0].toLowerCase()) {
                case "toggle":
                    Diana.toggle =! Diana.toggle;
                    Utils.sendModMessage("Toggled mod " + config.understandMe(Diana.toggle));
                    config.writeBooleanConfig("toggles", "ModToggle", Diana.toggle);
                    break;

                case "guess":
                    Diana.guess =! Diana.guess;
                    Utils.sendModMessage("Toggled burrow guess " + config.understandMe(Diana.guess));
                    config.writeBooleanConfig("toggles", "GuessBurrow", Diana.guess);
                    break;

                case "interpolation":
                    Diana.interpolation =! Diana.interpolation;
                    Utils.sendModMessage("Toggled burrow guess interpolation " + config.understandMe(Diana.interpolation));
                    config.writeBooleanConfig("toggles", "Interpolation", Diana.interpolation);
                    break;

                case "proximity":
                    Diana.proximity =! Diana.proximity;
                    Utils.sendModMessage("Toggled burrow proximity finder " + config.understandMe(Diana.proximity));
                    config.writeBooleanConfig("toggles", "BurrowProximity", Diana.proximity);
                    break;

                case "messages":
                    Diana.messages =! Diana.messages;
                    Utils.sendModMessage("Toggled messages " + config.understandMe(Diana.messages));
                    config.writeBooleanConfig("toggles", "Messages", Diana.messages);
                    break;

                case "send":
                    Diana.sendInqToAll = !Diana.sendInqToAll;
                    Utils.sendModMessage("Inquis coords will be sent to " + allOrParty(Diana.sendInqToAll));
                    config.writeBooleanConfig("toggles", "SendInquisToAll", Diana.sendInqToAll);
                    break;

                case "receive":
                    Diana.receiveInqFromAll = !Diana.receiveInqFromAll;
                    Utils.sendModMessage("You will receive inquis coords from " + allOrParty(Diana.receiveInqFromAll));
                    config.writeBooleanConfig("toggles", "ReceiveInquisFromAll", Diana.receiveInqFromAll);
                    break;

                case "beacon":
                    if (strings.length > 1) {
                        switch (strings[1].toLowerCase()) {
                            case "block":
                                Diana.block =! Diana.block;
                                Utils.sendModMessage("Toggled beacon block " + config.understandMe(Diana.block));
                                config.writeBooleanConfig("toggles", "BeaconBlock", Diana.block);
                                break;

                            case "beam":
                                Diana.beam =! Diana.beam;
                                Utils.sendModMessage("Toggled beacon beam " + config.understandMe(Diana.beam));
                                config.writeBooleanConfig("toggles", "BeaconBeam", Diana.beam);
                                break;

                            case "text":
                                Diana.text =! Diana.text;
                                Utils.sendModMessage("Toggled beacon text " + config.understandMe(Diana.text));
                                config.writeBooleanConfig("toggles", "BeaconText", Diana.text);
                                break;

                            default:
                                Utils.sendModMessage(new ChatComponentText(beaconHelp()));
                        }
                    } else {
                        Utils.sendModMessage(new ChatComponentText(beaconHelp()));
                    }
                    break;

                case "ignore":
                    if (strings.length == 1) {
                        Utils.sendModMessage("Ignore List: " + Diana.ignoredPlayers);
                    } else if (strings.length == 2) {
                        if (strings[1].equalsIgnoreCase("list") || strings[1].equalsIgnoreCase("view")) {
                            Utils.sendModMessage("Ignore List: " + Diana.ignoredPlayers);
                        } else {
                            Utils.sendModMessage("§cInvalid args!§r /diana ignore [list, add [player], remove [player]]");
                        }
                    } else {
                        switch (strings[1].toLowerCase()) {
                            case "list":
                            case "view":
                                Utils.sendModMessage("Ignore List: " + Diana.ignoredPlayers);
                                break;

                            case "add":
                                List<String> players1 = Utils.getArgsAfter(strings, 2);
                                for (String p : players1) {
                                    if (!Diana.ignoredPlayers.contains(p)) Diana.ignoredPlayers.add(p);
                                }
                                Utils.sendModMessage("Added " + Arrays.toString(players1.toArray()) + " to your ignore List");
                                config.writeStringListConfig("data", "InquisIgnoreList", Diana.ignoredPlayers);
                                break;

                            case "remove":
                                List<String> players2 = Utils.getArgsAfter(strings, 2);
                                Diana.ignoredPlayers.removeAll(players2);
                                Utils.sendModMessage("Removed " + Arrays.toString(players2.toArray()) + " from your ignore List");
                                config.writeStringListConfig("data", "InquisIgnoreList", Diana.ignoredPlayers);
                                break;

                            default:
                                Utils.sendModMessage("§cInvalid args!§r /diana ignore [list, add [player], remove [player]]");
                        }
                    }
                    break;

                case "clear":
                    Diana.waypoints = new HashMap<>();
                    Utils.sendModMessage("Cleared Waypoints");
                    break;

                case "clearall":
                    Diana.waypoints = new HashMap<>();
                    Diana.foundBurrows = new ArrayList<>();
                    Utils.sendModMessage("Cleared Waypoints and found burrows");
                    break;

                case "reload":
                    config.cfgreload();
                    Utils.sendModMessage("Reloaded config");
                    break;

                case "dev":
                    switch (strings[1].toLowerCase()) {
                        case "test":
                            if (strings.length == 5) {
                                try {
                                    int x = Integer.parseInt(strings[2]);
                                    int y = Integer.parseInt(strings[3]);
                                    int z = Integer.parseInt(strings[4]);
                                    Diana.burrow = new Vec3(x, y, z);
                                    Utils.sendModMessage("Set burrow to: " + x + "," + y + "," + z);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;

                        case "playsound":
                            if (strings.length == 5) {
                                try {
                                    Utils.playSound(strings[2], Float.parseFloat(strings[3]), Float.parseFloat(strings[4]));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;

                        case "listparty":
                            Utils.sendModMessage("Party: " + Diana.inParty + " " + Diana.partyMembers);
                            break;
                    }
                    break;

                default:
                    Utils.sendModMessage(new ChatComponentText(help()));
            }
        }
    }
}
