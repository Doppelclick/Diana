package com.Diana.mod.utils;

import com.Diana.mod.Diana;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    //from Danker's Skyblock Mod under GNU 3.0 license
    public static boolean checkHub() {
        Collection<NetworkPlayerInfo> players = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
        for (NetworkPlayerInfo player : players) {
            if (player == null || player.getDisplayName() == null) continue;
            String text = player.getDisplayName().getUnformattedText();
            if (text.startsWith("Area: ")) {
                return text.substring(text.indexOf(":") + 2).equals("Hub");
            }
        }
        return false;
    }

    public static String getLocation() {
        String location = "Unknown";
        List<String> scoreboard = getSidebarLines();
        for (String s : scoreboard) {
            String sCleaned = StringUtils.stripControlCodes(s).replace("⚽", "");
            if (sCleaned.contains("⏣")) {
                Matcher loc = Pattern.compile("⏣ (?<location>\\S+((\\s\\S+)?))").matcher(sCleaned);
                if (loc.find()) {
                    location = loc.group("location");
                    if (location.contains("Museum")) location = "Museum"; //to remove the "AAA's" Museum
                }
            };
        }
        return location;
    }

    //The following function was taken from DungeonRooms under the GNU 3.0 license
    public static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        if (Minecraft.getMinecraft().theWorld == null) return lines;
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) return lines;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return lines;

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null &! input.getPlayerName()
                        .startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
        }

        return lines;
    }
}
