package com.johnymuffin.jperms.beta;

import com.johnymuffin.jperms.beta.config.JPermsLanguage;
import com.johnymuffin.jperms.beta.config.PermissionsConfig;
import com.johnymuffin.jperms.beta.objects.Group;
import com.johnymuffin.jperms.core.models.PermissionsGroup;
import com.johnymuffin.jperms.core.models.PermissionsUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JohnyPerms extends JavaPlugin {
    //Basic Plugin Info
    private static JohnyPerms plugin;
    private Logger log;
    private String pluginName;
    private PluginDescriptionFile pdf;
    private int debugLevel = 3;
    //Plugin Stuff
    private JPermsLanguage language;
    private PermissionsGroup defaultGroup; //Default player group
    private HashMap<String, PermissionsGroup> groups = new HashMap<>(); //Groups
    private HashMap<UUID, PermissionsUser> users = new HashMap<>(); //Users
    private HashMap<UUID, PermissionAttachment> attachments = new HashMap<>(); //Permissions Attachments
    private PermissionsConfig permissionsConfig; //Plugin Perms Store
    private HashMap<String, Boolean> allPluginPerms = new HashMap<>();


    @Override
    public void onEnable() {
        plugin = this;
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();
        log.info("[" + pluginName + "] Is Loading, Version: " + pdf.getVersion());
        log.info("[" + pluginName + "] Loading Language File.");
        this.language = new JPermsLanguage(new File(plugin.getDataFolder(), "language.yml"));
        log.info("[" + pluginName + "] Loading Permissions Database.");
        this.permissionsConfig = new PermissionsConfig(plugin);
        if (permissionsConfig.isNew()) {
            log.info("[" + pluginName + "] Generating default group as it is first load.");
            PermissionsGroup group = new Group(plugin, "default", new JSONObject());
            group.setIsDefaultGroup(true);
            group.setSaveStatus(true);
            groups.put("default", group);
        }
//        plugin.logMessage(Level.INFO, "Importing PermissionsEX");
//        PexImport pexImport = new PexImport(plugin);
//        pexImport.importGroups();
//        scanForDefault();
//        try {
//            pexImport.importPlayers();
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//        plugin.logMessage(Level.INFO, "Finished importing PermissionsEx");
        log.info("[" + pluginName + "] Loading all permissions for wildcards.");
        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            for (Permission permission : plugin.getDescription().getPermissions()) {
                allPluginPerms.put(permission.getName(), true);
                for (Map.Entry<String, Boolean> entry : permission.getChildren().entrySet()) {
                    allPluginPerms.put(entry.getKey(), entry.getValue());
                }
            }
        }
        log.info("[" + pluginName + "] Registering Commands.");
        plugin.getCommand("jperms").setExecutor(new JohnyPermsCommand(plugin));
        log.info("[" + pluginName + "] Loading Groups.");
        for (String groupName : permissionsConfig.getGroupNames()) {
            PermissionsGroup group = permissionsConfig.getGroup(groupName);
            if (group == null) {
                log.info("[" + pluginName + "] group " + groupName + " failed to load, shutting down.");
                Bukkit.getServer().getPluginManager().disablePlugin(this);
                return;
            }
            groups.put(groupName, group);
            log.info("[" + pluginName + "] Loaded group " + groupName);
        }
        scanForDefault();
        if (this.defaultGroup == null) {
            log.info("[" + pluginName + "] Failed to find a default group, shutting down.");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            log.info("[" + pluginName + "] Default group set to: " + this.defaultGroup.getName());
        }
        log.info("[" + pluginName + "] Starting player listener.");
        JohnyPermsListener johnyPermsListener = new JohnyPermsListener(plugin);
        Bukkit.getServer().getPluginManager().registerEvents(johnyPermsListener, plugin);


        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            recalculatePlayer(p);
        }


    }

    @Override
    public void onDisable() {
        log.info("[" + pluginName + "] Plugin disabling.");

        for (Map.Entry<String, PermissionsGroup> entry : this.groups.entrySet()) {
            if (entry.getValue().isSaving()) {
                this.permissionsConfig.saveGroup(entry.getValue());
                log.info("[" + pluginName + "] Saved group called: " + entry.getKey());
            }
        }

        for (Map.Entry<UUID, PermissionsUser> entry : this.users.entrySet()) {
            if (entry.getValue().isSaving()) {
                this.permissionsConfig.saveUser(entry.getValue());
                log.info("[" + pluginName + "] Saved user: " + entry.getKey());
            }
        }


        //Remove permission attachments
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            removePlayer(p);
        }
        log.info("[" + pluginName + "] Plugin disabled.");

    }

    public void scanForDefault() {
        for (String group : this.getGroups().keySet()) {
            if (this.groups.get(group).isDefaultGroup()) {
                this.defaultGroup = this.groups.get(group);
            }
        }
    }

    public void logMessage(Level level, String msg) {
        log.log(level, "[" + pluginName + "] " + msg);
    }

    public PermissionsUser getUser(UUID uuid) {
        if (users.containsKey(uuid)) return users.get(uuid);
        PermissionsUser user = this.permissionsConfig.getUser(uuid);
        users.put(uuid, user);
        return user;
    }

    public HashMap<String, PermissionsGroup> getGroups() {
        return groups;
    }

    public HashMap<UUID, PermissionsUser> getUsers() {
        return users;
    }

    public PermissionsGroup getDefaultGroup() {
        return defaultGroup;
    }

    public HashMap<UUID, PermissionAttachment> getAttachments() {
        return attachments;
    }

    public JPermsLanguage getLanguage() {
        return language;
    }

    public PermissionsConfig getPermissionsConfig() {
        return permissionsConfig;
    }

    public HashMap<String, Boolean> getAllPluginPerms() {
        return allPluginPerms;
    }

    public void recalculatePlayer(Player p) {
        //Remove attachment if a phantom one exists
        if (plugin.getAttachments().containsKey(p.getUniqueId())) {
            plugin.getAttachments().remove(p.getUniqueId());
        }
        PermissionAttachment attachment = p.addAttachment(plugin);
        plugin.getAttachments().put(p.getUniqueId(), attachment);

        for (String key : attachment.getPermissions().keySet()) {
            attachment.unsetPermission(key);
        }

        for (Map.Entry<String, Boolean> permission : plugin.getUser(p.getUniqueId()).getPermissions().entrySet()) {
            if (permission.getKey() == null || permission.getValue() == null) {
                continue;
            }
            attachment.setPermission(permission.getKey(), permission.getValue());
        }

        p.recalculatePermissions();
    }

    public void removePlayer(Player player) {
        if (plugin.getAttachments().containsKey(player.getUniqueId())) {
            try {
                player.removeAttachment(plugin.getAttachments().get(player.getUniqueId()));
            } catch (IllegalArgumentException ex) {
            }
            plugin.getAttachments().remove(player.getUniqueId());
        }
    }
}
