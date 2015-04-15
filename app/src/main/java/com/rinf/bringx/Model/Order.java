package com.rinf.bringx.Model;

import org.json.JSONObject;

public class Order {

    private static final String KEY_RESSTATUS = "status";
    private static final String KEY_DATA = "data";

    // Order
    private static final String KEY_ORDERID = "DeliveryOrderId";
    private static final String KEY_UID = "uid";
    private static final String KEY_DELIVERYPRICE = "Delivery";
    private static final String KEY_ITEMS = "Order";

    // Items to deliver
    private static final String KEY_ITEM_COUNT = "count";
    private static final String KEY_ITEM_TITLE = "title";
    private static final String KEY_PRICE = "Price";

    // Meeting
    private static final String KEY_TYPE = "DeliveryType";
    private static final String KEY_STATUS = "DrpStatus";
    private static final String KEY_ADDRESS = "Address";
    private static final String KEY_ADDR_NAME = "Name";
    private static final String KEY_ADDR_COMPANY = "Company";
    private static final String KEY_ADDR_STREET = "Street";
    private static final String KEY_ADDR_CITY = "City";
    private static final String KEY_ADDR_ZIP = "Zip";
    private static final String KEY_ADDR_PHONE = "Phone";

    private static final String KEY_ETA = "EstimatedArrival";
    private static final String KEY_DELAY = "EstimatedDelay";

    private static final String KEY_NOTES = "Notes";
    private static final String KEY_INSTRUCTIONS = "Instructions";

    private static final String KEY_POSITION = "Position";
    private static final String KEY_POSX = "X";
    private static final String KEY_POSY = "Y";

    private static final String KEY_COUNT = "Count";

    public Order(JSONObject src) {

    }

    private String _orderId;

    public String OrderId() {
        return _orderId;
    }
}