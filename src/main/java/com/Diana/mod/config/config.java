package com.Diana.mod.config;

import com.Diana.mod.Diana;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Taken from DungeonRooms under Creative Commons Attribution-NonCommercial 3.0
 * https://github.com/Quantizr/DungeonRoomsMod/blob/3.x/LICENSE
 * @author Quantizr
 */

public class config {
    public static Configuration config;
    private final static String file = "config/Diana.cfg";


    public static void init() {
        config = new Configuration(new File(file));
        try {
            config.load();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static boolean getBoolean(String category, String key) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.getCategory(category).containsKey(key)) {
                return config.get(category, key, false).getBoolean();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return true;
    }

    public static ArrayList<String> getStringList(String category, String key) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (config.getCategory(category).containsKey(key)) {
                return new ArrayList<>(Arrays.asList(config.get(category, key, new String[0]).getStringList()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return new ArrayList<>();
    }

    public static void writeBooleanConfig(String category, String key, boolean value) {
        config = new Configuration(new File(file));
        try {
            config.load();
            config.get(category, key, value).getBoolean();
            config.getCategory(category).get(key).set(value);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static void writeStringListConfig(String category, String key, List<String> value) {
        config = new Configuration(new File(file));
        try {
            String[] write = value.stream().map(String::toString).toArray(String[]::new);
            config.load();
            config.get(category, key, write).getStringList();
            config.getCategory(category).get(key).set(write);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
    }

    public static boolean hasKey(String category, String key) {
        config = new Configuration(new File(file));
        try {
            config.load();
            if (!config.hasCategory(category)) return false;
            return config.getCategory(category).containsKey(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            config.save();
        }
        return false;
    }

    public static String understandMe(boolean c) {
        return (c?"§2On":"§4Off") + "§r";
    }

    public static void cfgreload() {
        init();

        if (!hasKey("toggles", "ModToggle")) writeBooleanConfig("toggles", "ModToggle", false);
        if (!hasKey("toggles", "GuessBurrow")) writeBooleanConfig("toggles", "GuessBurrow", true);
        if (!hasKey("toggles", "Interpolation")) writeBooleanConfig("toggles", "Interpolation", true);
        if (!hasKey("toggles", "BurrowProximity")) writeBooleanConfig("toggles", "BurrowProximity", true);
        if (!hasKey("toggles", "Messages")) writeBooleanConfig("toggles", "Messages", false);
        if (!hasKey("toggles", "SendInquisToAll")) writeBooleanConfig("toggles", "SendInquisToAll", false);
        if (!hasKey("toggles", "ReceiveInquisFromAll")) writeBooleanConfig("toggles", "ReceiveInquisFromAll", true);

        if (!hasKey("render", "BeaconBlock")) writeBooleanConfig("render", "BeaconBlock", true);
        if (!hasKey("render", "BeaconBeam")) writeBooleanConfig("render", "BeaconBeam", true);
        if (!hasKey("render", "BeaconText")) writeBooleanConfig("render", "BeaconText", true);

        if (!hasKey("warps", "castle")) writeBooleanConfig("warps", "castle", true);
        if (!hasKey("warps", "da")) writeBooleanConfig("warps", "da", true);
        if (!hasKey("warps", "crypt")) writeBooleanConfig("warps", "crypt", true);
        if (!hasKey("warps", "museum")) writeBooleanConfig("warps", "museum", true);
        if (!hasKey("warps", "wizard")) writeBooleanConfig("warps", "wizard", true);

        if (!hasKey("data", "InquisIgnoreList")) writeStringListConfig("data", "InquisIgnoreList", new ArrayList<>());

        Diana.toggle = getBoolean("toggles", "ModToggle");
        Diana.guess = getBoolean("toggles", "GuessBurrow");
        Diana.interpolation = getBoolean("toggles", "Interpolation");
        Diana.proximity = getBoolean("toggles", "BurrowProximity");
        Diana.messages = getBoolean("toggles", "Messages");
        Diana.sendInqToAll = getBoolean("toggles", "SendInquisToAll");
        Diana.receiveInqFromAll = getBoolean("toggles", "ReceiveInquisFromAll");

        Diana.block = getBoolean("render", "BeaconBlock");
        Diana.beam = getBoolean("render", "BeaconBeam");
        Diana.text = getBoolean("render", "BeaconText");

        if (!getBoolean("warps", "castle")) Diana.Warp.set("castle", false);
        if (!getBoolean("warps", "da")) Diana.Warp.set("da", false);
        if (!getBoolean("warps", "crypt")) Diana.Warp.set("crypt", false);
        if (!getBoolean("warps", "museum")) Diana.Warp.set("museum", false);
        if (!getBoolean("warps", "wizard")) Diana.Warp.set("wizard", false);

        Diana.ignoredPlayers = getStringList("data", "InquisIgnoreList");

        Diana.logger.info("Reloaded config");
    }
}
