package com.rinf.bringx.storage;

import com.rinf.bringx.utils.Log;

public class SettingsStorage extends StorageManager {

    private final String MultipleValuesSeparator = "\\|";

    public static String PENDING_STATUSES = "pending-statuses";
    public static String LAST_MEETINGS_ORDER = "meetings-order";

    public static String APP_VERSION = "app-version";
    public static String REG_ID = "registration-id";
    public static String FIST_MEETING_CHANGED = "first-meeting-changed";

    public SettingsStorage(String user) {
        super(user);
    }

    @Override
    protected String getStorageKey() {
        return "com.ring.bringx.STORAGE_SETTINGS_" + _userName;
    }

    @Override
    protected Object getTypedValue(String value) {
        return value;
    }

    public void removeFromKey(String key, String value) {
        String oldValue = super.getString(key);

        if (oldValue.isEmpty())
            return;

        String[] newKV = value.split(",");
        String newValue = "";
        Boolean newValueFound = false;

        String[] pairs = oldValue.split(MultipleValuesSeparator);
        for (String pair : pairs) {
            String[] kv = pair.split(",");

            if (newValue.isEmpty() == false) {
                newValue += "|";
            }

            if (kv[0].equals(newKV[0]) && kv[1].equals(newKV[1]) && newValueFound == false) {
                newValueFound = true;
            } else {
                // Continue rewriting
                newValue += kv[0] + ',' + kv[1];
            }
        }

        if (newValueFound)
            setString(key, newValue);
    }

    public void appendToKey(String key, String value) {
        String oldValue = super.getString(key);

        String[] newKV = value.split(",");
        String newValue = "";
        Boolean newValueFound = false;

        if (oldValue.isEmpty()) {
            newValue = newKV[0] + "," + newKV[1];
        } else {
            String[] pairs = oldValue.split(MultipleValuesSeparator);
            for (String pair : pairs) {
                String[] kv = pair.split(",");

                if (newValue.isEmpty() == false) {
                    newValue += "|";
                }

                if (kv[0].equals(newKV[0]) && newValueFound == false) {
                    // Write new status
                    newValue += kv[0] + "," + newKV[1];
                    newValueFound = true;
                } else {
                    // Continue rewriting
                    newValue += kv[0] + ',' + kv[1];
                }
            }

            if (newValueFound == false) {
                newValue += "|" + newKV[0] + "," + newKV[1];
            }
        }

        setString(key, newValue);
    }
}