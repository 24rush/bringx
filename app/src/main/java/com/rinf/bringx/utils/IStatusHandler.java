package com.rinf.bringx.utils;

import org.json.JSONObject;

public interface IStatusHandler<Type, Params> {
    public void OnError(Error err, Params... ctx);
    public void OnSuccess(Type response, Params... ctx);
}
