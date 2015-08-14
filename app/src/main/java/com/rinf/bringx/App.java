package com.rinf.bringx;

import android.app.Application;
import android.content.Context;

import com.rinf.bringx.utils.DeviceManager;
import com.rinf.bringx.utils.Requester;
import com.rinf.bringx.storage.Storage;

import java.util.Timer;
import java.util.TimerTask;

public class App extends Application {

    private static Context _context;
    private static Storage _storage;
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

    public static Storage StorageManager() {
        if (_storage == null) {
            _storage = new Storage();
        }

        return App._storage;
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

    public static boolean IsVisible() {
        return activityVisible;
    }

    public static void OnActivityResumed() {
        activityVisible = true;
    }

    public static void OnActivityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;

    private static Timer mActivityTransitionTimer;
    private static TimerTask mActivityTransitionTimerTask;
    public static boolean wasInBackground;
    private static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    public static void startActivityTransitionTimer() {
        mActivityTransitionTimer = new Timer();
        mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                wasInBackground = true;
            }
        };

        mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public static void stopActivityTransitionTimer() {
        if (mActivityTransitionTimerTask != null) {
            mActivityTransitionTimerTask.cancel();
        }

        if (mActivityTransitionTimer != null) {
            mActivityTransitionTimer.cancel();
        }

        wasInBackground = false;
    }
}