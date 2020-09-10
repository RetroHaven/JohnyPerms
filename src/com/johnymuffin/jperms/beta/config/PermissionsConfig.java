package com.johnymuffin.jperms.beta.config;

import com.johnymuffin.jperms.beta.objects.Group;
import com.johnymuffin.jperms.beta.JohnyPerms;
import com.johnymuffin.jperms.beta.objects.User;
import com.johnymuffin.jperms.core.models.PermissionsGroup;
import com.johnymuffin.jperms.core.models.PermissionsUser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class PermissionsConfig {
    protected File configFile;
    protected JSONObject jsonConfig;
    private JohnyPerms plugin;
    private boolean isNew = false;

    public PermissionsConfig(JohnyPerms plugin) {
        this.configFile = new File(plugin.getDataFolder(), "permissions.json");
        this.plugin = plugin;
        //Create directory
        if (!this.configFile.exists()) {
            isNew = true;
            this.configFile.getParentFile().mkdirs();
            jsonConfig = new JSONObject();
            saveFile();
        } else {
            try {
                JSONParser parser = new JSONParser();
                jsonConfig = (JSONObject) parser.parse(new FileReader(configFile));
            } catch (ParseException e) {
                System.out.println("Failed to load config file.");
                throw new RuntimeException(e + ": " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e + ": " + e.getMessage());
            }
        }
        saveFile();
    }

    public PermissionsUser getUser(UUID uuid) {
        JSONObject tmp = getPlayers();
        if (tmp.containsKey(String.valueOf(uuid))) {
            try {
                PermissionsUser user = new User(plugin, (JSONObject) tmp.get(String.valueOf(uuid)), uuid);
                return user;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new User(plugin, uuid);
    }

    public void saveUser(PermissionsUser permissionsUser) {
        JSONObject tmp = new JSONObject();
        JSONArray tmp2 = new JSONArray();
        tmp.put("group", permissionsUser.getGroup().getName());
        for (Map.Entry<String, Boolean> entry : permissionsUser.getPermissions(false).entrySet()) {
            if (entry.getValue()) {
                tmp2.add(entry.getKey());
            } else {
                tmp2.add("-" + entry.getKey());
            }
        }
        tmp.put("permissions", tmp2);
        if (permissionsUser.getPrefix() != null && !permissionsUser.getPrefix().isEmpty()) {
            tmp.put("prefix", permissionsUser.getPrefix());
        }
        if (permissionsUser.getSuffix() != null && !permissionsUser.getSuffix().isEmpty()) {
            tmp.put("suffix", permissionsUser.getSuffix());
        }
        JSONObject tmp3 = getPlayers();
        tmp3.put(String.valueOf(permissionsUser.getUUID()), tmp);
        savePlayers(tmp3);

    }

    private JSONObject getPlayers() {
        if (this.jsonConfig.containsKey("players")) {
            return (JSONObject) this.jsonConfig.get("players");
        }
        return new JSONObject();
    }

    private void savePlayers(JSONObject players) {
        this.jsonConfig.put("players", players);
        this.saveFile();
    }

    public void saveGroup(PermissionsGroup permissionsGroup) {
        JSONObject tmp = new JSONObject();
        JSONArray tmp2 = new JSONArray();
        for (Map.Entry<String, Boolean> entry : permissionsGroup.getPermissions(false).entrySet()) {
            if (entry.getValue()) {
                tmp2.add(entry.getKey());
            } else {
                tmp2.add("-" + entry.getKey());
            }
        }
        tmp.put("permissions", tmp2);
        if (permissionsGroup.getPrefix() != null && !permissionsGroup.getPrefix().isEmpty()) {
            tmp.put("prefix", permissionsGroup.getPrefix());
        }
        if (permissionsGroup.getSuffix() != null && !permissionsGroup.getSuffix().isEmpty()) {
            tmp.put("suffix", permissionsGroup.getSuffix());
        }
        tmp.put("default", permissionsGroup.isDefaultGroup());

        JSONArray tmp4 = new JSONArray();
        for (String inheritance : permissionsGroup.getRawInheritanceGroups()) {
            if (inheritance != null && !inheritance.isEmpty())
                tmp4.add(inheritance.toLowerCase());
        }
        tmp.put("inheritance", tmp4);
        JSONObject tmp3 = getGroups();
        tmp3.put(permissionsGroup.getName().toLowerCase(), tmp);
        saveGroups(tmp3);
    }

    public PermissionsGroup getGroup(String name) {
        JSONObject tmp = getGroups();
        if (tmp.containsKey(name.toLowerCase())) {
            try {
                PermissionsGroup group = new Group(plugin, name.toLowerCase(), (JSONObject) tmp.get(name.toLowerCase()));
                return group;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String[] getGroupNames() {
        JSONObject tmp = getGroups();
        String[] groups = new String[tmp.size()];
        int count = 0;
        for (Object entry : tmp.keySet()) {
            groups[count] = String.valueOf(entry);
            count = count + 1;
        }
        return groups;
    }

    public JSONObject getGroups() {
        if (this.jsonConfig.containsKey("groups")) {
            return (JSONObject) this.jsonConfig.get("groups");
        }
        return new JSONObject();
    }

    private void saveGroups(JSONObject groups) {
        this.jsonConfig.put("groups", groups);
        this.saveFile();
    }


    protected void saveFile() {
        try (FileWriter file = new FileWriter(configFile)) {
            file.write(jsonConfig.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isNew() {
        return isNew;
    }
}
