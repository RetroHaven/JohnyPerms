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

import static com.johnymuffin.jperms.beta.util.Util.convertPermission;
import static com.johnymuffin.jperms.beta.util.Util.hasPermissionOnMap;

public class User implements PermissionsUser, PermissionsObject, PermissionsAesthetics, SavableObject {
    private UUID uuid;
    private String lastKnownUsername;
    private HashMap<String, Boolean> permissions = new HashMap<>();
    private JohnyPerms plugin;
    private PermissionsGroup group;
    private boolean isModified = false;
    private boolean isNew;

    public User(JohnyPerms plugin, JSONObject playerData, UUID uuid, boolean isNew) {
        this.plugin = plugin;
        this.isNew = isNew;
        this.permissions = Util.getPermissions((JSONArray) playerData.getOrDefault("permissions", new JSONArray()));
        this.uuid = uuid;
        String rawGroupName = String.valueOf(playerData.getOrDefault("group", plugin.getDefaultGroup().getName()));
        lastKnownUsername = String.valueOf(playerData.get("lastKnownUsername"));
        if (plugin.getGroups().containsKey(rawGroupName)) {
            this.group = plugin.getGroups().get(rawGroupName);
        } else {
            this.group = plugin.getDefaultGroup();
        }

    }

    public User(JohnyPerms plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.isNew = true;
    }

    public UUID getUUID() {
        return uuid;
    }

    public HashMap<String, Boolean> getPermissions() {
        return getPermissions(true);
    }

    public HashMap<String, Boolean> getPermissions(boolean deepSearch) {
        if (!deepSearch) {
            return (HashMap<String, Boolean>) permissions.clone();
        }
        //Fetch group permissions
        HashMap<String, Boolean> temp = (HashMap<String, Boolean>) this.getGroup().getPermissions().clone();
        //Override group perms with user perms
        for (String permission : this.permissions.keySet()) {
            temp.put(permission, this.permissions.get(permission));
        }
        //Convert appropriate wildcard permissions to the appropriate perms
        for (Map.Entry<String, Boolean> perm : plugin.getAllPluginPerms().entrySet()) {
            if (hasPermissionOnMap(perm.getKey(), this.permissions)) {
                temp.put(perm.getKey(), perm.getValue());
            }
        }

        return temp;
    }

    public PermissionsGroup getGroup() {
        if (group == null) return plugin.getDefaultGroup();
        return group;
    }

    public void setGroup(PermissionsGroup group) {
        this.group = group;
        this.isModified = true;
    }

    public boolean hasPermission(String permission) {
        HashMap<String, Boolean> temp = this.getPermissions();
        if (temp.containsKey(permission)) return temp.get(permission);
        return false;
    }

    public boolean hasPermission(String permission, boolean deepSearch) {
        throw new RuntimeException("Not implemented yet");
    }

    public boolean hasPermissionSomehow(String permission, boolean deepSearch) {
        //TODO: These methods need to be sorted out, hasPermission() only supports wildcards for registered perms, and hasPermissionSomehow() supports any wildcards.
        //TODO: A cache needs to be implemented so checks don't need to be run everytime.
//        PermissionNode permissionNode = convertPermission(permission);
//        if (!(plugin.getAllPluginPerms().containsKey(permissionNode.getPermission()) && plugin.getAllPluginPerms().get(permissionNode.getPermission()) == permissionNode.isValue())) {
//            plugin.getAllPluginPerms().put(permissionNode.getPermission(), permissionNode.isValue());
//        }
        return hasPermissionOnMap(permission, this.getPermissions(deepSearch));
    }

    public void addPermission(String permission, boolean value) {
        permissions.put(permission, value);
        isModified = true;
    }

    public void addPermission(String permission) {
        PermissionNode permissionNode = convertPermission(permission);
        addPermission(permissionNode.getPermission(), permissionNode.isValue());
    }

    public boolean removePermission(String permission, boolean value) {
        boolean removed = permissions.remove(permission, value);
        isModified = true;
        return removed;
    }

    public boolean removePermission(String permission) {
        PermissionNode permissionNode = convertPermission(permission);
        return removePermission(permissionNode.getPermission(), permissionNode.isValue());
    }

    public String getPrefix() {
        return null;
    }

    public String getSuffix() {
        return null;
    }

    public void setPrefix(String prefix) {

    }

    public void setSuffix(String suffix) {

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

    public String getLastKnownUsername() {
        return lastKnownUsername;
    }

    public void setLastKnownUsername(String lastKnownUsername) {
        if (this.getGroup() == plugin.getDefaultGroup()) {
            this.lastKnownUsername = lastKnownUsername;
            return;
        }
        if (this.getLastKnownUsername() != null) {
            if (this.getLastKnownUsername().equals(lastKnownUsername)) {
                return;
            }
        }
        this.lastKnownUsername = lastKnownUsername;
        this.isModified = true;
    }
}
