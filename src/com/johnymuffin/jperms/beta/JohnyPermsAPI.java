package com.johnymuffin.jperms.beta;

import com.johnymuffin.jperms.core.models.PermissionsGroup;
import com.johnymuffin.jperms.core.models.PermissionsUser;

import java.util.UUID;

public class JohnyPermsAPI {
    private JohnyPerms plugin;

    public JohnyPermsAPI(JohnyPerms plugin) {
        this.plugin = plugin;
    }

    public PermissionsUser getUser(UUID uuid) {
        return plugin.getUser(uuid);
    }

    public PermissionsGroup getGroup(String name) {
        return plugin.getGroups().get(name);
    }


}
