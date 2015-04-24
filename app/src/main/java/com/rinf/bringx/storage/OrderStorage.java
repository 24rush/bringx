package com.rinf.bringx.storage;

import com.rinf.bringx.Model.Order;

import org.json.JSONException;
import org.json.JSONObject;

public class OrderStorage extends StorageManager<Order> {

    public OrderStorage(String user) {
        super(user);
    }

    @Override
    protected String getStorageKey() {
        return "com.ring.bringx.STORAGE_ORDERS" + "_" + _userName;
    }

    @Override
    protected Order getTypedValue(String value) {
        try {
            return new Order(new JSONObject(value));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}