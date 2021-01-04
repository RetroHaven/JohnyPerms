package com.johnymuffin.jperms.core.models;

import java.util.HashMap;

public interface PermissionsObject {

    public HashMap<String, Boolean> getPermissions();

    public void addPermission(String permission, boolean value);

    public void addPermission(String permission);

    public boolean removePermission(String permission, boolean value);

    public boolean removePermission(String permission);

    public boolean hasPermission(String permission);

    public boolean hasPermission(String permission, boolean deepSearch);

}
