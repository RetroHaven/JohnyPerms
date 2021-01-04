package com.johnymuffin.jperms.core.models;

import java.util.HashMap;
import java.util.UUID;

public interface PermissionsUser extends PermissionsAesthetics, PermissionsObject, SavableObject {

    public UUID getUUID();

    public PermissionsGroup getGroup();

    public void setGroup(PermissionsGroup group);

    public boolean hasPermission(String permission);

    public HashMap<String, Boolean> getPermissions(boolean deepSearch);

    public boolean hasPermissionSomehow(String permission, boolean deepSearch);


}
