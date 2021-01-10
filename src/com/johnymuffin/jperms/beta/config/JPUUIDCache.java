package com.johnymuffin.jperms.beta.config;


import com.johnymuffin.jperms.beta.JohnyPerms;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class JPUUIDCache {
    private JohnyPerms plugin;
    private JSONObject usernameUUIDCache;
    private File cacheFile;
    private boolean memoryOnly = false;

    public JPUUIDCache(JohnyPerms plugin) {
        this.plugin = plugin;
        cacheFile = new File(plugin.getDataFolder(), "UUIDCache.json");
        if (!cacheFile.exists()) {
            cacheFile.getParentFile().mkdirs();
            try {
                FileWriter file = new FileWriter(cacheFile);
                plugin.logMessage(Level.INFO, "Generating UUIDCache.json file");
                usernameUUIDCache = new JSONObject();
                file.write(usernameUUIDCache.toJSONString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            plugin.logMessage(Level.INFO, "Reading UUIDCache.json file");
            JSONParser parser = new JSONParser();
            usernameUUIDCache = (JSONObject) parser.parse(new FileReader(cacheFile));
        } catch (ParseException e) {
            plugin.logMessage(Level.WARNING, "UUIDCache.json file is corrupt, resetting file: " + e + " : " + e.getMessage());
            usernameUUIDCache = new JSONObject();
        } catch (Exception e) {
            plugin.logMessage(Level.WARNING, "UUIDCache.json file is corrupt, changing to memory only mode.");
            memoryOnly = true;
            usernameUUIDCache = new JSONObject();
        }


    }

    public void addUser(String username, UUID uuid) {
        usernameUUIDCache.put(username.toLowerCase(), uuid.toString());
    }

    public UUID getUUIDFromUsername(String username) {
        username = username.toLowerCase();
        if (usernameUUIDCache.containsKey(username)) {
            return UUID.fromString(String.valueOf(usernameUUIDCache.get(username)));
        }
        return null;
    }


    public void saveData() {
        saveJsonArray();
    }

    private void saveJsonArray() {
        if (memoryOnly) {
            return;
        }
        try (FileWriter file = new FileWriter(cacheFile)) {
            plugin.logMessage(Level.INFO, "Saving UUIDCache.json");
            file.write(usernameUUIDCache.toJSONString());
            file.flush();
        } catch (IOException e) {
            plugin.logMessage(Level.WARNING, "Error saving UUIDCache.json: " + e + " : " + e.getMessage());
        }
    }

}
