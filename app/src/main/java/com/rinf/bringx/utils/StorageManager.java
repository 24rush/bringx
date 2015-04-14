package com.rinf.bringx.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.rinf.bringx.App;

public class StorageManager {
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _editor;

    public StorageManager() {
        _sharedPrefs = App.getAppContext().getSharedPreferences("BRINGX_CACHE", Context.MODE_PRIVATE);
        _editor = _sharedPrefs.edit();
    }

    public String getString(String key) {
        return _sharedPrefs.getString(key, "");
    }

    public void setString(String key, String value) {
        _editor.putString(key, value);
        _editor.commit();
    }
}
