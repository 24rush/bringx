package com.rinf.bringx;

import android.app.Application;
import android.content.Context;

import com.rinf.bringx.utils.StorageManager;

public class App extends Application {

    private static Context _context;
    private static StorageManager _storageManager;

    @Override
    public void onCreate(){
        super.onCreate();
        App._context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App._context;
    }

    public static StorageManager StorageManager() {
        if (_storageManager == null) {
            _storageManager = new StorageManager();
        }

        return App._storageManager;
    }
}