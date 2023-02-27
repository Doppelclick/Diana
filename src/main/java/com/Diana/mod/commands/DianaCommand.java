package com.Diana.mod.commands;

import com.Diana.mod.Diana;
import com.Diana.mod.config.config;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import java.util.List;

public class DianaCommand extends CommandBase {
    static String help = "§3Diana Solver\n§r"
            + " /diana help | This message\n"
            + " /diana toggle | Toggle the mod\n"
            + " /diana guess | Toggle burrow guess\n"
            + " /diana proximity | Toggle burrow\n"
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


    public List<String> addTabCompletionOptions(ICommandSender sender, String[] strings) {
        if (strings.length == 1) {
            return getListOfStringsMatchingLastWord(strings, "help", "toggle", "guess", "proximity", "beacon");
        }
        if ((strings.length == 2) && strings[1].equalsIgnoreCase("beacon")) {
            return getListOfStringsMatchingLastWord(strings, "help", "block", "beam", "text");
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] strings) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = (EntityPlayer) sender;
        if (strings.length < 1) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN
                            + "[ZT] For available commands use \"/zt help\"."));
        } else {
            switch (strings[0].toLowerCase()) {
                case "toggle":
                    Diana.toggle=!Diana.toggle;
                    config.writeBooleanConfig("toggles", "ModToggle", Diana.toggle);
                    break;

                case "guess":
                    Diana.guess=!Diana.guess;
                    config.writeBooleanConfig("toggles", "GuessBurrow", Diana.guess);
                    break;

                case "proximity":
                    Diana.proximity=!Diana.proximity;
                    config.writeBooleanConfig("toggles", "BurrowProximity", Diana.proximity);
                    break;

                case "beacon":
                    if (strings.length > 1) {
                        switch (strings[1].toLowerCase()) {
                            case "block":
                                Diana.block=!Diana.block;
                                config.writeBooleanConfig("toggles", "BeaconBlock", Diana.block);
                                break;

                            case "beam":
                                Diana.beam=!Diana.beam;
                                config.writeBooleanConfig("toggles", "BeaconBeam", Diana.beam);
                                break;

                            case "text":
                                Diana.text=!Diana.text;
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
                    mc.thePlayer.addChatMessage(new ChatComponentText(help));
            }
        }
    }
}
