package com.Diana.mod.config;

import com.Diana.mod.Diana;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

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

    public static void writeBooleanConfig(String category, String key, boolean value) {
        config = new Configuration(new File(file));
        try {
            config.load();
            boolean set = config.get(category, key, value).getBoolean();
            config.getCategory(category).get(key).set(value);
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

    public static void cfgreload() {
        init();

        if (!hasKey("toggles", "ModToggle")) writeBooleanConfig("toggles", "ModToggle", false);
        if (!hasKey("toggles", "GuessBurrow")) writeBooleanConfig("toggles", "GuessBurrow", false);
        if (!hasKey("toggles", "BurrowProximity")) writeBooleanConfig("toggles", "BurrowProximity", false);

        if (!hasKey("render", "BeaconBlock")) writeBooleanConfig("render", "BeaconBlock", true);
        if (!hasKey("render", "BeaconBeam")) writeBooleanConfig("render", "BeaconBeam", true);
        if (!hasKey("render", "BeaconText")) writeBooleanConfig("render", "BeaconText", true);

        Diana.toggle = getBoolean("toggles", "ModToggle");
        Diana.guess = getBoolean("toggles", "GuessBurrow");
        Diana.proximity = getBoolean("toggles", "BurrowProximity");

        Diana.block = getBoolean("render", "BeaconBlock");
        Diana.beam = getBoolean("render", "BeaconBeam");
        Diana.text = getBoolean("render", "BeaconText");
    }
}
