package com.rinf.bringx.utils;

import org.json.JSONObject;

public interface IStatusHandler {
    public void OnError(Error err);
    public void OnSuccess(JSONObject response);
}
