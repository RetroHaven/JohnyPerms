package com.johnymuffin.jperms.core.models;

import java.util.HashMap;

public interface PermissionsGroup extends PermissionsAesthetics, PermissionsObject, SavableObject {


    public String getName();

    public PermissionsGroup[] getInheritanceGroups();

    public String[] getRawInheritanceGroups();

    public void setRawInheritanceGroups(String[] groups);

    public PermissionsUser[] getUsers();

    public HashMap<String, Boolean> getPermissions(boolean deepSearch);

    public boolean isDefaultGroup();

    public void setIsDefaultGroup(boolean value);


}
