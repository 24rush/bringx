package com.rinf.bringx.utils;

import org.json.JSONObject;

public class Error {
    public int Code;
    public String Message;

    public Error(int code, String msg) {
        Code = code;
        Message = msg;
    }
}