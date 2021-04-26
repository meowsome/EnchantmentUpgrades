package me.meowso.enchantmentupgrades;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Updater {
    public String getVersion() throws IOException {
        URL url = new URL("https://api.github.com/repos/meowsome/EnchantmentUpgrades/releases");
        URLConnection request = url.openConnection();
        request.connect();

        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); // Convert the input stream to a json element
        JsonArray rootobj = (JsonArray) root; // Convert to array

        // If no results, return null. If results, get name attribute of array index 0
        return rootobj.size() > 0 ? ((JsonObject)rootobj.get(0)).get("name").getAsString() : null;
    }

    public boolean isNewVersion(String newVersion, String oldVersion) {
        return !newVersion.equalsIgnoreCase(oldVersion);
    }
}