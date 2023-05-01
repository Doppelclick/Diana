package com.Diana.mod.utils;

import com.Diana.mod.Diana;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Utils {
    public static JsonObject readJsonObject(File file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean writeJsonObject(File file, JsonObject obj) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(obj, writer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getArgsAfter(String[] strings, int in) {
        List<String> args = new ArrayList<>();
        for (int i = in; i < strings.length; i++) {
            args.add(strings[i]);
        }
        return args;
    }

    public static void sendModMessage(String msg) {
        sendModMessage(new ChatComponentText(Diana.chatTitle + msg));
    }
    public static void sendModMessage(IChatComponent msg) {
        Diana.mc.thePlayer.addChatMessage(msg);
    }

    public static void showClientTitle(String title, String subtitle) {
        Diana.mc.ingameGUI.displayTitle(null, null, 2, 40, 2); //set timings
        Diana.mc.ingameGUI.displayTitle(null, subtitle, -1, -1, -1); //do subtitle
        Diana.mc.ingameGUI.displayTitle(title, null, -1, -1, -1); //do title
    }

    public static void ping() {
        Utils.playSound("note.pling", 1, 0.6f);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Utils.playSound("note.pling", 1, 0.7f);
            }
        }, 180);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Utils.playSound("note.pling", 1, 0.8f);
            }
        }, 360);
    }

    public static void playSound(String sound, float volume, float pitch) {
        Diana.mc.thePlayer.playSound(sound, volume, pitch);
    }
}
