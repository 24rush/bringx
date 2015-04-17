package com.rinf.bringx.storage;

import com.rinf.bringx.storage.CredentialsStorage;

public class Storage {
    private CredentialsStorage _credentialsStorage;
    private OrderStorage _orderStorage;

    public CredentialsStorage Credentials() {
        if (_credentialsStorage == null) {
            _credentialsStorage = new CredentialsStorage();
        }

        return _credentialsStorage;
    }

    public OrderStorage Orders() {
        if (_orderStorage == null) {
            _orderStorage = new OrderStorage();
        }

        return _orderStorage;
    }
}
