package com.johnymuffin.jperms.core.models;

import java.util.HashMap;

public interface PermissionsObject {

    public HashMap<String, Boolean> getPermissions();

    public void addPermission(String permission, boolean value);

    public void addPermission(String permission);

    public void removePermission(String permission, boolean value);

    public void removePermission(String permission);

}
