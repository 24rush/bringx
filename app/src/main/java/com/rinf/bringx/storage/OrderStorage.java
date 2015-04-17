package com.rinf.bringx.storage;

import com.rinf.bringx.Model.Order;

public class OrderStorage extends StorageManager<Order> {

    @Override
    protected String getStorageKey() {
        return "com.ring.bringx.STORAGE_ORDERS";
    }
}