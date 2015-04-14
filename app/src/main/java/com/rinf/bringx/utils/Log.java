package com.rinf.bringx.utils;

public class Log {
    public static void d(String message) {
        android.util.Log.d("[bringx]", message);
    }

    public static void e(String message) {
        android.util.Log.e("[bringx]", message);
    }
}