package com.rinf.bringx.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.rinf.bringx.App;
import com.rinf.bringx.utils.Log;

import java.util.HashMap;
import java.util.Map;

public abstract class StorageManager <Type> {
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _editor;

    protected String _userName = "";
    protected abstract String getStorageKey();
    protected abstract Type getTypedValue(String value);

    public StorageManager(String userName) {
        _userName = userName;
        _sharedPrefs = App.Context().getSharedPreferences(getStorageKey(), Context.MODE_PRIVATE);
        _editor = _sharedPrefs.edit();
    }

    public String getString(String key) {
        return _sharedPrefs.getString(key, "");
    }

    public void setString(String key, String value) {
        Log.d("Saving " + key + " " + value);
        _editor.putString(key, value);
        _editor.commit();
    }

    public Map<String, Type> getAll() {
        Map<String, Type> typedValue = new HashMap<String, Type>();
        Map<String, ?> keyValues = _sharedPrefs.getAll();

        for (String key : keyValues.keySet()) {
            typedValue.put(key, getTypedValue((String)keyValues.get(key)));
        }

        return typedValue;
    }
}