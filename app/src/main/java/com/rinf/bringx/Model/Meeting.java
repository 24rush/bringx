package com.rinf.bringx.Model;

import com.rinf.bringx.utils.Log;

import org.json.JSONObject;

import java.util.Date;

public class Meeting {
    public String OrderID;
    public String OrderVersion;
    public Date ETAPickup = null;
    public Date ETADelivery = null;

    public Meeting(String orderId, String orderVersion, Date etaPickup, Date etaDelivery) {
        OrderID = orderId;
        OrderVersion = orderVersion;
        ETAPickup = etaPickup;
        ETADelivery = etaDelivery;

        Log.d("Created meeting with: " + orderId + " " + orderVersion + " " + ETAPickup + " " + ETADelivery);
    }
}
