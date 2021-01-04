package com.johnymuffin.jperms.beta.override;

import com.johnymuffin.jperms.beta.JohnyPerms;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;

import java.util.UUID;

public class JPPermissibleBase extends PermissibleBase {
    private UUID playerUUID;
    private String lastPermissionCheck;

    public JPPermissibleBase(Player player, ServerOperator operator) {
        super(operator);
        this.playerUUID = player.getUniqueId();
    }

    @Override
    public boolean hasPermission(String permission) {
        lastPermissionCheck = permission;
        if (JohnyPerms.getJPermsAPI().getUser(playerUUID).hasPermissionSomehow(permission, true)) return true;
        return super.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        if (hasPermission(perm.getName())) {
            return true;
        }
        return super.hasPermission(perm);
    }
}
