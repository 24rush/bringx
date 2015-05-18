package com.rinf.bringx.utils;

public interface IStatusHandler<Type, Params> {
    public void OnError(com.rinf.bringx.utils.Error err, Params... ctx);
    public void OnSuccess(Type response, Params... ctx);
}
