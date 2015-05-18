package com.rinf.bringx.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.rinf.bringx.App;
import com.rinf.bringx.utils.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class StorageManager<Type> {
    private SharedPreferences _sharedPrefs;
    private SharedPreferences.Editor _editor;

    private AsyncFileSave _asyncFileSave;
    private BlockingQueue<Integer> _bq = new LinkedBlockingQueue<Integer>(100);

    protected String _userName = "";

    protected abstract String getStorageKey();

    protected abstract Type getTypedValue(String value);

    public StorageManager(String userName) {
        _userName = userName;
        _sharedPrefs = App.Context().getSharedPreferences(getStorageKey(), Context.MODE_PRIVATE);
        _editor = _sharedPrefs.edit();
    }

    private void save() {
        _bq.add(0);

        if (_asyncFileSave == null || _asyncFileSave.getStatus() == AsyncTask.Status.FINISHED) {
            _asyncFileSave = new AsyncFileSave(_editor, _bq);
            _asyncFileSave.execute();
        }
    }

    public String getString(String key) {
        return _sharedPrefs.getString(key, "");
    }

    public void setString(String key, String value) {
        _editor.putString(key, value);
        save();
    }

    public void setInt(String key, int value) {
        _editor.putInt(key, value);
        save();
    }

    public int getInt(String key) {
        return _sharedPrefs.getInt(key, Integer.MIN_VALUE);
    }

    public Map<String, Type> getAll() {
        Map<String, Type> typedValue = new HashMap<String, Type>();
        Map<String, ?> keyValues = _sharedPrefs.getAll();

        for (String key : keyValues.keySet()) {
            typedValue.put(key, getTypedValue((String) keyValues.get(key)));
        }

        return typedValue;
    }

    public Boolean getBoolean(String key) {
        return _sharedPrefs.getBoolean(key, false);
    }

    public void setBoolean(String key, Boolean value) {
        _editor.putBoolean(key, value);
        save();
    }

    public void remove(String key) {
        _editor.remove(key);
        save();
    }
}

class AsyncFileSave extends AsyncTask<Void, Void, Boolean> {
    private SharedPreferences.Editor _sharedPrefEditor;
    private BlockingQueue<Integer> _bq;

    public AsyncFileSave(SharedPreferences.Editor editor, BlockingQueue<Integer> bq) {
        _sharedPrefEditor = editor;
        _bq = bq;
    }

    @Override
    protected Boolean doInBackground(Void[] params) {
        while (!_bq.isEmpty()) {
            try {
                _bq.take();
                _sharedPrefEditor.commit();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}