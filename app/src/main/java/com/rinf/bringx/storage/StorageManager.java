package com.rinf.bringx.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.rinf.bringx.App;

import java.util.Map;

public abstract class StorageManager <Type> {
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _editor;

    protected abstract String getStorageKey();

    public StorageManager() {
        _sharedPrefs = App.Context().getSharedPreferences(getStorageKey(), Context.MODE_PRIVATE);
        _editor = _sharedPrefs.edit();
    }

    public String getString(String key) {
        return _sharedPrefs.getString(key, "");
    }

    public void setString(String key, String value) {
        _editor.putString(key, value);
        _editor.commit();
    }

    public Map<String, ?> getAll() {
        return _sharedPrefs.getAll();
    }
}