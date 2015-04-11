package com.rinf.bringx.utils;

import android.content.Context;

public class Localization {
    private Context mContext;

    public Localization(Context ctx) {
        mContext = ctx;
    }

    public String getText(int id) {
        return mContext.getResources().getString(id);
    }
}
