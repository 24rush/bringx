package com.rinf.bringx.utils;

import org.json.JSONObject;

public interface IStatusHandler<Type> {
    public void OnError(Error err);
    public void OnSuccess(Type response);
}
