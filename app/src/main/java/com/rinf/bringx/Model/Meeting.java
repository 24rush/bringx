package com.rinf.bringx.Model;

import org.json.JSONObject;

import java.util.Date;

public class Meeting {
    public String OrderID;
    public String OrderVersion;
    public Date ETAPickup;
    public Date ETADelivery;

    private final String KEY_ORDER_ID_VERSION = "order_uid1-versionnumber";
    private final String KEY_ETA_PICKUP = "eta1_pickup";
    private final String KEY_ETA_DELIVERY = "";

    public static Meeting fromJSONObject(JSONObject obj) {
        return null;
    }
}
