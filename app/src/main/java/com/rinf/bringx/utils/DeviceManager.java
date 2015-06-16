package com.rinf.bringx.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.Controls;
import com.rinf.bringx.storage.SettingsStorage;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceManager {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SENDER_ID = "842130361712";

    private String _deviceId = null;

    private GoogleCloudMessaging _gcm;
    private AtomicInteger _msgId = new AtomicInteger();

    public String DeviceId() {
        //_deviceId = "158a-4141-a32e-6eb2";
        if (_deviceId != null)
            return _deviceId;

        _deviceId = getUniqueID(App.Context());

        if (_deviceId == null)
            _deviceId = Settings.Secure.getString(App.Context().getContentResolver(), Settings.Secure.ANDROID_ID);

        return _deviceId;
    }

    public boolean IsNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.Context().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static String getUniqueID(Context context) {

        String telephonyDeviceId = "NoTelephonyId";
        String androidDeviceId = "NoAndroidId";

        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyDeviceId = tm.getDeviceId();

            if (telephonyDeviceId == null)
                telephonyDeviceId = "NoTelephonyId";

        } catch (Exception e) {
        }

        // get internal android device id
        try {
            androidDeviceId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            if (androidDeviceId == null)
                androidDeviceId = "NoAndroidId";

        } catch (Exception e) {
        }

        // build up the uuid
        try {
            String id = getStringIntegerHexBlocks(androidDeviceId.hashCode())
                    + "-"
                    + getStringIntegerHexBlocks(telephonyDeviceId.hashCode());

            return id;

        } catch (Exception e) {
            return "0000-0000-1111-1111";
        }
    }

    private static String getStringIntegerHexBlocks(int value) {
        String result = "";
        String string = Integer.toHexString(value);

        int remain = 8 - string.length();
        char[] chars = new char[remain];
        Arrays.fill(chars, '0');
        string = new String(chars) + string;

        int count = 0;
        for (int i = string.length() - 1; i >= 0; i--) {
            count++;
            result = string.substring(i, i + 1) + result;
            if (count == 4) {
                result = "-" + result;
                count = 0;
            }
        }

        if (result.startsWith("-")) {
            result = result.substring(1, result.length());
        }

        return result;
    }

    private boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(App.Context());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e("This device is not supported.");
            }

            return false;
        }

        return true;
    }

    public void RegisterForPushNotifications(Activity activity) {
        if (checkPlayServices(activity)) {
            _gcm = GoogleCloudMessaging.getInstance(App.Context());
            String regId = getRegistrationId(App.Context());

            if (regId.isEmpty()) {
                if (_gcm == null) {
                    _gcm = GoogleCloudMessaging.getInstance(App.Context());
                }

                try {
                    regId = _gcm.register(SENDER_ID);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d("Device registered, registration ID=" + regId);

                App.StorageManager().Setting().setString(SettingsStorage.REG_ID, regId);
                App.StorageManager().Setting().setInt(SettingsStorage.APP_VERSION, getAppVersion(App.Context()));
            }
        }
    }

    private String getRegistrationId(Context context) {
        String registrationId = App.StorageManager().Setting().getString(SettingsStorage.REG_ID);

        if (registrationId.isEmpty()) {
            Log.d("Registration not found.");
            return "";
        }

        int registeredVersion = App.StorageManager().Setting().getInt(SettingsStorage.APP_VERSION);
        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion) {
            Log.d("App version changed.");
            return "";
        }

        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    if (_gcm == null) {
                        _gcm = GoogleCloudMessaging.getInstance(App.Context());
                    }

                    String regId = _gcm.register(SENDER_ID);

                    Log.d("Device registered, registration ID=" + regId);

                    App.StorageManager().Setting().setString(SettingsStorage.REG_ID, regId);
                    App.StorageManager().Setting().setInt(SettingsStorage.APP_VERSION, getAppVersion(App.Context()));

                } catch (IOException ex) {
                    Log.e(ex.getMessage());
                }

                return 0;
            }
        }.execute(null, null, null);
    }
}
