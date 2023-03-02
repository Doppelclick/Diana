package com.Diana.mod.commands;

import com.Diana.mod.Diana;
import com.Diana.mod.config.config;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class DianaCommand extends CommandBase {
    static String help = "§3Diana Solver\n§r"
            + " /diana help | This message\n"
            + " /diana toggle | Toggle the mod\n"
            + " /diana guess | Toggle burrow guess\n"
            + " /diana proximity | Toggle burrow\n"
            + " /diana messages | Toggle messages\n"
            + " /diana beacon [help, block, beam, text]";

    static String beaconHelp = "§3Diana Solver §r beacon options\n"
            + " /diana beacon help | This message\n"
            + " /diana beacon block | Toggle beacon block\n"
            + " /diana beacon beam | Toggle beacon beam\n"
            + " /diana beacon text | Toggle beacon text";

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
            return getListOfStringsMatchingLastWord(strings, "help", "toggle", "guess", "proximity", "messages", "beacon");
        }
        if ((strings.length == 2) && strings[1].equalsIgnoreCase("beacon")) {
            return getListOfStringsMatchingLastWord(strings, "help", "block", "beam", "text");
        }
        return null;
    }
    @Override
    public void processCommand(ICommandSender sender, String[] strings) {
        EntityPlayer player = (EntityPlayer) sender;
        if (strings.length < 1) {
            player.addChatMessage(new ChatComponentText("§3[Diana] §rFor available commands use \"/diana help\"."));
        } else {
            switch (strings[0].toLowerCase()) {
                case "toggle":
                    Diana.toggle=!Diana.toggle;
                    Diana.mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §rToggled mod " + config.understandMe(Diana.toggle)));
                    config.writeBooleanConfig("toggles", "ModToggle", Diana.toggle);
                    break;

                case "guess":
                    Diana.guess=!Diana.guess;
                    Diana.mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §rToggled burrow guess " + config.understandMe(Diana.guess)));
                    config.writeBooleanConfig("toggles", "GuessBurrow", Diana.guess);
                    break;

                case "proximity":
                    Diana.proximity=!Diana.proximity;
                    Diana.mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §rToggled burrow proximity finder " + config.understandMe(Diana.proximity)));
                    config.writeBooleanConfig("toggles", "BurrowProximity", Diana.proximity);
                    break;

                case "messages":
                    Diana.messages=!Diana.messages;
                    Diana.mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §rToggled messages " + config.understandMe(Diana.messages)));
                    config.writeBooleanConfig("toggles", "Messages", Diana.messages);
                    break;

                case "beacon":
                    if (strings.length > 1) {
                        switch (strings[1].toLowerCase()) {
                            case "block":
                                Diana.block=!Diana.block;
                                Diana.mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §rToggled beacon block " + config.understandMe(Diana.block)));
                                config.writeBooleanConfig("toggles", "BeaconBlock", Diana.block);
                                break;

                            case "beam":
                                Diana.beam=!Diana.beam;
                                Diana.mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §rToggled beacon beam " + config.understandMe(Diana.beam)));
                                config.writeBooleanConfig("toggles", "BeaconBeam", Diana.beam);
                                break;

                            case "text":
                                Diana.text=!Diana.text;
                                Diana.mc.thePlayer.addChatMessage(new ChatComponentText("§3[Diana] §rToggled beacon text " + config.understandMe(Diana.text)));
                                config.writeBooleanConfig("toggles", "BeaconText", Diana.text);
                                break;

                            default:
                                player.addChatMessage(new ChatComponentText(beaconHelp));
                                break;
                        }
                    } else {
                        player.addChatMessage(new ChatComponentText(beaconHelp));
                    }
                    break;

                default:
                    Diana.mc.thePlayer.addChatMessage(new ChatComponentText(help));
            }
        }
    }
}
