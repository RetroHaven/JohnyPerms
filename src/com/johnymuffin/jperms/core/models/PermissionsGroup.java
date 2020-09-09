package com.johnymuffin.jperms.core.models;

import java.util.HashMap;

public interface PermissionsGroup extends PermissionsAesthetics, PermissionsObject, SavableObject {


    public String getName();

    public PermissionsGroup[] getInheritanceGroups();

    public PermissionsUser[] getUsers();

    public HashMap<String, Boolean> getPermissions(boolean deepSearch);

    public boolean isDefaultGroup();


}
