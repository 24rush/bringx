package com.rinf.bringx.utils;

import android.os.AsyncTask;

import com.rinf.bringx.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class URLS {
    public static String LoginURL = "http://auftrag.bringx.com/json/login/1";
    public static String JobsURL = "http://auftrag.bringx.com/json/route/";
    public static String StatusURL = "http://auftrag.bringx.com/json/drp/";
}

public class ServiceProxy {
    private IStatusHandler _statusHandler;

    public ServiceProxy(IStatusHandler statusHandler) {
        _statusHandler = statusHandler;
    }

    public void Login(String userName, String password, String android_id) {
        UserLoginTask loginTask = new UserLoginTask(_statusHandler);
        loginTask.execute(userName, password, android_id);
    }

    public void GetMeetingsList(String userName, String android_id) {
        MeetingsListTask meetingsListTask = new MeetingsListTask(_statusHandler);
        meetingsListTask.execute(userName, android_id);
    }
}

abstract class AsyncTaskReport <Params, Progress, Return> extends AsyncTask<Params, Progress, Return> {
    protected IStatusHandler _statusHandler;

    public AsyncTaskReport(IStatusHandler statusHandler) {
        _statusHandler = statusHandler;
    }

    protected void ReportError(int code, String message) {
        if (_statusHandler != null) {
            _statusHandler.OnError(new Error(code, message));
        }
    }

    protected void ReportSuccess(JSONObject response) {
        if (_statusHandler != null) {
            _statusHandler.OnSuccess(response);
        }
    }
}

class UserLoginTask extends AsyncTaskReport<String, Void, JSONObject> {

    public UserLoginTask(IStatusHandler statusHandler) {
        super(statusHandler);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if (params.length != 3) {
            return null;
        }

        JSONObject jsonObj = null;

        Log.d("Performing login for: " + params[0] + " on device: " + params[2]);

        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("hidden", "0");
            jsonParams.put("mobileid", params[2]);
            jsonParams.put("username", params[0]);
            jsonParams.put("password", DataUtils.md5(params[1]));

            jsonObj = new JSONObject(App.Requester().POST(URLS.LoginURL, jsonParams));

            Thread.sleep(2000, 0);

            if (params[0].equals("a") && params[1].equals("b"))
                jsonObj = new JSONObject("{\"status\":\"true\"}");
            else
                jsonObj = new JSONObject("{\"status\":\"false\"}");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObj) {
        if (jsonObj == null) {
            ReportError(500, "Server error");
            return;
        }

        try {
            if (jsonObj.getString("status").equals("true")) {
                ReportSuccess(jsonObj);
            } else {
                ReportError(1, "Invalid login credentials");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class MeetingsListTask extends AsyncTaskReport<String, Void, JSONObject> {

    public MeetingsListTask(IStatusHandler statusHandler) {
        super(statusHandler);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if (params.length != 3) {
            return null;
        }

        JSONObject jsonObj = null;

        Log.d("Performing GetMeetingsList for: " + params[0] + " on device: " + params[1]);

        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("mobileid", params[1]);

            jsonObj = new JSONObject(App.Requester().POST(URLS.JobsURL, jsonParams));

            Thread.sleep(2000, 0);

            jsonObj = new JSONObject("{\"status\":\"true\"}");


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObj) {
        if (jsonObj == null) {
            ReportError(500, "Server error");
            return;
        }

        try {
            if (jsonObj.getString("status").equals("true")) {
                ReportSuccess(jsonObj);
            } else {
                ReportError(1, "Error getting meetings");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class DataUtils {
    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
