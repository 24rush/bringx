package com.rinf.bringx.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.rinf.bringx.App;

import java.util.Arrays;

public class DeviceManager {

    private String _deviceId = null;

    public String DeviceId() {
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
}
