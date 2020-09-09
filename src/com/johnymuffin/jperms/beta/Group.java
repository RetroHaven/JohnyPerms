package com.johnymuffin.jperms.beta;

import com.johnymuffin.jperms.core.models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.UUID;

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
            }
        }

        return temp;
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
            for (String permission : group.getPermissions(false).keySet()) {
                temp.put(permission, group.getPermissions(false).get(permission));
            }
        }
        //Current Group Permissions
        for (String permission : this.getPermissions().keySet()) {
            temp.put(permission, this.getPermissions().get(permission));
        }

        return temp;
    }

    public boolean isDefaultGroup() {
        return this.defaultGroup;
    }

    public void addPermission(String permission, boolean value) {
        permissions.put(permission, value);
        isModified = true;
    }

    public void addPermission(String permission) {
        PermissionNode permissionNode = Util.convertPermission(permission);
        addPermission(permissionNode.getPermission(), permissionNode.isValue());
    }

    public void removePermission(String permission, boolean value) {
        permissions.remove(permission, value);
        isModified = true;
    }

    public void removePermission(String permission) {
        PermissionNode permissionNode = Util.convertPermission(permission);
        removePermission(permissionNode.getPermission(), permissionNode.isValue());
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
