package com.rinf.bringx.utils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

public class ServiceProxy {
    private IStatusHandler _statusHandler;

    public ServiceProxy(IStatusHandler statusHandler) {
        _statusHandler = statusHandler;
    }

    public void Login(String userName, String password, String android_id) {
        UserLoginTask loginTask = new UserLoginTask(_statusHandler);
        loginTask.execute(userName, password, android_id);
    }
}

class UserLoginTask extends AsyncTask<String, Void, JSONObject> {

    private IStatusHandler _statusHandler;

    public UserLoginTask(IStatusHandler statusHandler) {
        _statusHandler = statusHandler;
    }

    private void ReportError(int code, String message) {
        if (_statusHandler != null) {
            _statusHandler.OnError(new Error(code, message));
        }
    }

    private void ReportSuccess(JSONObject response) {
        if (_statusHandler != null) {
            _statusHandler.OnSuccess(response);
        }
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
        //String jsonStr = userFunction.loginUser(params[0], params[1], params[2]);
        JSONObject jsonObj = null;

        try {
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
