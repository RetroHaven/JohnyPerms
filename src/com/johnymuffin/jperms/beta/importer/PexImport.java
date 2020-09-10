package com.johnymuffin.jperms.beta.importer;

import com.johnymuffin.jperms.beta.objects.Group;
import com.johnymuffin.jperms.beta.JohnyPerms;
import com.johnymuffin.jperms.beta.util.UUIDFetcher2;
import com.johnymuffin.jperms.beta.util.Util;
import com.johnymuffin.jperms.core.models.PermissionsGroup;
import com.johnymuffin.jperms.core.models.PermissionsUser;
import com.projectposeidon.api.PoseidonUUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PexImport {
    private JohnyPerms plugin;

    public PexImport(JohnyPerms plugin) {
        this.plugin = plugin;
    }


    public Map<String, UUID> fetchUUIDs() throws Exception {
        PermissionManager permissionManager = PermissionsEx.getPermissionManager();
        int count = 0;
        String[] usernames = new String[permissionManager.getUsers().length];
        JSONArray tmp = new JSONArray();
        for (PermissionUser pexUser : permissionManager.getUsers()) {
            usernames[count] = pexUser.getName();
            count = count + 1;
            tmp.add(pexUser.getName());
        }
        System.out.println(tmp.toJSONString());
        UUIDFetcher2 uuidFetcher = new UUIDFetcher2(Arrays.asList(usernames), true);
        return uuidFetcher.call();

    }


    public void importGroups() {
        PermissionManager permissionManager = PermissionsEx.getPermissionManager();
        for (PermissionGroup pexGroup : permissionManager.getGroups()) {
            PermissionsGroup jpermsGroup = new Group(plugin, pexGroup.getName().toLowerCase(), new JSONObject());
            //Copy suffix and prefix over
            jpermsGroup.setPrefix(pexGroup.getOwnPrefix());
            jpermsGroup.setSuffix(pexGroup.getOwnSuffix());
            for (World worldr : Bukkit.getServer().getWorlds()) {
                String world = worldr.getName();
                //Save is default
                if (pexGroup.isDefault(world)) {
                    jpermsGroup.setIsDefaultGroup(true);
                }
            }
            //Copy permissions over
            for (Map.Entry<String, String[]> entry : pexGroup.getAllPermissions().entrySet()) {
                for (String permission : entry.getValue()) {
                    jpermsGroup.addPermission(permission);
                }
            }
            //Copy inherited groups
            for (String groupName : pexGroup.getParentGroupsNames()) {
                String[] oldParents = jpermsGroup.getRawInheritanceGroups();
                String[] newParents = new String[oldParents.length + 1];
                int count = 0;
                for (String old : oldParents) {
                    newParents[0] = old;
                    count = count + 1;
                }
                newParents[count] = groupName;
                jpermsGroup.setRawInheritanceGroups(newParents);
            }
            jpermsGroup.setSaveStatus(true);
            plugin.getGroups().put(pexGroup.getName().toLowerCase(), jpermsGroup);
            plugin.logMessage(Level.INFO, "Imported a group called " + jpermsGroup.getName() + " from PEX.");
        }
    }

    public void importPlayers() throws Exception {
        int count = 0;
        PermissionManager permissionManager = PermissionsEx.getPermissionManager();
        Map<String, UUID> uuidMap = fetchUUIDs();
        for (PermissionUser pexUser : permissionManager.getUsers()) {
            UUID uuid = Util.getUUIDFromUsername(pexUser.getName());
            if (uuid == null) {
                if (uuidMap.containsKey(pexUser.getName())) {
                    uuid = uuidMap.get(pexUser.getName());
                } else {
                    plugin.logMessage(Level.WARNING, "Using offline UUID for player " + pexUser.getName());
                    uuid = PoseidonUUID.getPlayerGracefulUUID(pexUser.getName());
                }
            }
            PermissionsUser jPermsUser = plugin.getUser(uuid);
            //Import one group
            if (pexUser.getGroups().length >= 1) {
                String groupName = pexUser.getGroups()[0].getName();
                if (plugin.getGroups().containsKey(groupName.toLowerCase())) {
                    PermissionsGroup group = plugin.getGroups().get(groupName.toLowerCase());
                    jPermsUser.setGroup(group);
                } else {
                    plugin.logMessage(Level.WARNING, "Failed to add the group called " + groupName.toLowerCase() + " to " + pexUser.getName() + " as it couldn't be found.");
                }
            } else {
                plugin.logMessage(Level.WARNING, "Failed to find any groups for " + pexUser.getName() + ", assuming default group.");
            }

            jPermsUser.setSaveStatus(true);

        }
        if (count > 0) {
            plugin.logMessage(Level.WARNING, "Failed to import " + count + " players as UUID couldn't be found.");
        }
    }


}
