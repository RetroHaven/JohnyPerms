package com.johnymuffin.jperms.beta.objects;

import com.johnymuffin.jperms.beta.JohnyPerms;
import com.johnymuffin.jperms.beta.util.PermissionNode;
import com.johnymuffin.jperms.beta.util.Util;
import com.johnymuffin.jperms.core.models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static com.johnymuffin.jperms.beta.util.Util.hasPermissionOnMap;

public class Group implements PermissionsGroup, PermissionsObject, PermissionsAesthetics, SavableObject {
    private JohnyPerms plugin;
    private String groupName;
    private String prefix;
    private String suffix;
    private boolean defaultGroup;
    private String[] inheritance;
    private HashMap<String, Boolean> permissions = new HashMap<>();
    private boolean isModified = false;


    public Group(JohnyPerms plugin, String groupName, JSONObject groupData) {
        this.plugin = plugin;
        this.groupName = groupName;
        if (groupData.containsKey("prefix")) {
            this.prefix = String.valueOf(groupData.get("prefix"));
        }
        if (groupData.containsKey("suffix")) {
            this.suffix = String.valueOf(groupData.get("suffix"));
        }
        defaultGroup = Boolean.valueOf(String.valueOf(groupData.getOrDefault("default", false)));
        if (groupData.containsKey("inheritance")) {
            JSONArray inheritanceRaw = (JSONArray) groupData.get("inheritance");
            inheritance = new String[inheritanceRaw.size()];
            for (int i = 0; i < inheritanceRaw.size(); i++) {
                inheritance[i] = String.valueOf(inheritanceRaw.get(i));
            }
        } else {
            inheritance = new String[0];
        }
        this.permissions = Util.getPermissions((JSONArray) groupData.getOrDefault("permissions", new JSONArray()));
    }

    public String getName() {
        return groupName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.isModified = true;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.isModified = true;
    }

    public PermissionsGroup[] getInheritanceGroups() {
        int count = 0;
        for (int i = 0; i < inheritance.length; i++) {
            if (plugin.getGroups().containsKey(inheritance[i])) {
                count = count + 1;
            }
        }
        PermissionsGroup[] temp = new PermissionsGroup[count];
        for (int i = 0; i < inheritance.length; i++) {
            if (plugin.getGroups().containsKey(inheritance[i])) {
                temp[i] = plugin.getGroups().get(inheritance[i]);
            } else {
                plugin.logMessage(Level.WARNING, "A invalid inheritance group called " + inheritance[i] + " is in the config for " + groupName);
            }
        }

        return temp;
    }

    public String[] getRawInheritanceGroups() {
        return inheritance.clone();
    }

    public void setRawInheritanceGroups(String[] groups) {
        this.inheritance = groups;
        this.isModified = true;
    }

    public PermissionsUser[] getUsers() {
        int count = 0;
        for (UUID uuid : plugin.getUsers().keySet()) {
            if (plugin.getUsers().get(uuid).getGroup() == this) {
                count = count + 1;
            }
        }
        PermissionsUser[] temp = new PermissionsUser[count];
        int count2 = 0;
        for (UUID uuid : plugin.getUsers().keySet()) {
            if (plugin.getUsers().get(uuid).getGroup() == this) {
                temp[count2] = plugin.getUsers().get(uuid);
                count2 = count2 + 1;
            }
        }

        return temp;
    }

    public HashMap<String, Boolean> getPermissions() {
        return this.getPermissions(true);
    }

    @Override
    public HashMap<String, Boolean> getPermissions(boolean deepSearch) {
        if (!deepSearch) {
            return (HashMap<String, Boolean>) permissions.clone();
        }
        HashMap<String, Boolean> temp = new HashMap<>();
        //Get inherited permissions
        for (PermissionsGroup group : this.getInheritanceGroups()) {
            HashMap<String, Boolean> groupPerms = group.getPermissions(true);
            for (String permission : groupPerms.keySet()) {
                temp.put(permission, groupPerms.get(permission));
            }
        }
        //Current Group Permissions
        for (String permission : this.permissions.keySet()) {
            temp.put(permission, this.permissions.get(permission));
        }

        //Convert appropriate wildcard permissions to the appropriate perms
        for (Map.Entry<String, Boolean> perm : plugin.getAllPluginPerms().entrySet()) {
            if (hasPermissionOnMap(perm.getKey(), this.permissions)) {
                temp.put(perm.getKey(), perm.getValue());
//                plugin.logMessage(Level.INFO, perm.getKey() + " has been added to " + groupName + " through a wildcard.");
            }
        }

        return temp;
    }

    public boolean isDefaultGroup() {
        return this.defaultGroup;
    }

    public void setIsDefaultGroup(boolean value) {
        this.defaultGroup = value;
        this.isModified = true;
    }

    public void addPermission(String permission, boolean value) {
        permissions.put(permission, value);
        isModified = true;
    }

    public void addPermission(String permission) {
        PermissionNode permissionNode = Util.convertPermission(permission);
        addPermission(permissionNode.getPermission(), permissionNode.isValue());
    }

    public boolean removePermission(String permission, boolean value) {
        boolean removal = permissions.remove(permission, value);
        isModified = true;
        return removal;
    }

    public boolean removePermission(String permission) {
        PermissionNode permissionNode = Util.convertPermission(permission);
        return removePermission(permissionNode.getPermission(), permissionNode.isValue());
    }


    public boolean hasPermission(String permission) {
        return hasPermission(permission, true);
    }


    public boolean hasPermission(String permission, boolean deepSearch) {
        return hasPermissionOnMap(permission, this.getPermissions(deepSearch));
    }

    public void saveObject() {
        this.isModified = true;
    }

    public boolean isSaving() {
        return this.isModified;
    }

    public void setSaveStatus(boolean value) {
        this.isModified = value;
    }
}
