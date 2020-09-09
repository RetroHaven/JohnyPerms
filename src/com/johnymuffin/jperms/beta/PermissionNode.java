package com.johnymuffin.jperms.beta;

public class PermissionNode {
    private final String permission;
    private final boolean value;

    public PermissionNode(String permission, boolean value) {
        this.permission = permission;
        this.value = value;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isValue() {
        return value;
    }
}