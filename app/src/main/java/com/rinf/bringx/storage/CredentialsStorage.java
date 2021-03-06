package com.rinf.bringx.storage;

public class CredentialsStorage extends StorageManager {

    public CredentialsStorage(String user) {
        super(user);
    }

    @Override
    protected String getStorageKey() {
        return "com.ring.bringx.STORAGE_CREDENTIALS";
    }

    @Override
    protected Object getTypedValue(String value) {
        return value;
    }
}