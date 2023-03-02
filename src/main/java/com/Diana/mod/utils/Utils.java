package com.Diana.mod.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

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
}
