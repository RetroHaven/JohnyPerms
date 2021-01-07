package com.johnymuffin.jperms.beta.util;

import com.projectposeidon.api.PoseidonUUID;
import com.projectposeidon.api.UUIDType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class Util {
    private static Pattern p = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    public static HashMap<String, Boolean> getPermissions(JSONArray permissions) {
        HashMap<String, Boolean> temp = new HashMap<>();
        for (int i = 0; i < permissions.size(); i++) {
            if (permissions == null) continue;
            PermissionNode node = convertPermission(String.valueOf(permissions.get(i)));
            temp.put(node.getPermission(), node.isValue());
        }
        return temp;
    }


    public static PermissionNode convertPermission(String permission) {
        if (permission.substring(0, 1).equalsIgnoreCase("-")) {
            permission = permission.substring(1);
            return new PermissionNode(permission, false);
        } else {
            return new PermissionNode(permission, true);
        }
    }

    public static Player getPlayerFromString(String name) {
        List<Player> players = Bukkit.matchPlayer(name);
        if (players.size() == 1) {
            return players.get(0);
        }
        return null;
    }

    public static UUID getUUIDFromUsername(String name) {
        Player player = getPlayerFromString(name);
        if (player != null) {
            return player.getUniqueId();
        }
        //Search Poseidon Cache
        UUIDType uuidType = PoseidonUUID.getPlayerUUIDCacheStatus(name);
        switch (uuidType) {
            case ONLINE:
                return PoseidonUUID.getPlayerUUIDFromCache(name, true);
            case OFFLINE:
                return PoseidonUUID.getPlayerUUIDFromCache(name, false);
        }
        if (p.matcher(name).matches()) {
            try {
                return UUID.fromString(name);
            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
        return null;

    }

    public static Boolean hasPermissionOnMap(String permission, Map<String, Boolean> map) {
        String node = permission;

        while (true) {
            int endIndex = node.lastIndexOf('.');
            if (endIndex == -1) {
                break;
            }

            node = node.substring(0, endIndex);
            if (!node.isEmpty()) {
                Boolean b = map.get(node + ".*");
                if (b != null) {
                    return b;
                }
            }
        }

        Boolean b = map.get("'*'");
        if (b != null) {
            return b;
        }

        b = map.get("*");
        if (b != null) {
            return b;
        }

        return false;
    }

}
