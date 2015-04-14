package com.rinf.bringx;

import android.app.Application;
import android.content.Context;

import com.rinf.bringx.utils.DeviceManager;
import com.rinf.bringx.utils.Requester;
import com.rinf.bringx.utils.StorageManager;

public class App extends Application {

    private static Context _context;
    private static StorageManager _storageManager;
    private static DeviceManager _deviceManager;
    private static Requester _requester;

    @Override
    public void onCreate(){
        super.onCreate();
        App._context = getApplicationContext();
    }

    public static Context Context() {
        return App._context;
    }

    public static StorageManager StorageManager() {
        if (_storageManager == null) {
            _storageManager = new StorageManager();
        }

        return App._storageManager;
    }

    public static DeviceManager DeviceManager() {
        if (_deviceManager == null) {
            _deviceManager = new DeviceManager();
        }

        return _deviceManager;
    }

    public static Requester Requester() {
        if (_requester == null) {
            _requester = new Requester();
        }

        return _requester;
    }
}