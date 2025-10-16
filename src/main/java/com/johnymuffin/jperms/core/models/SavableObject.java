package com.johnymuffin.jperms.core.models;

public interface SavableObject {

    public void saveObject();

    public boolean isSaving();

    public void setSaveStatus(boolean value);
}
