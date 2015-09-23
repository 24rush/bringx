package com.rinf.bringx.storage;

import com.rinf.bringx.storage.CredentialsStorage;

import java.security.InvalidParameterException;

public class Storage {
    private CredentialsStorage _credentialsStorage;
    private OrderStorage _orderStorage;
    private SettingsStorage _settingsStorage;
    private String _userName = "";

    public CredentialsStorage Credentials() {
        if (_credentialsStorage == null) {
            _credentialsStorage = new CredentialsStorage(_userName);
        }

        return _credentialsStorage;
    }

    public OrderStorage Orders() {
        if (_userName.isEmpty())
            throw new InvalidParameterException("No username set");

        if (_orderStorage == null) {
            _orderStorage = new OrderStorage(_userName);
        }

        return _orderStorage;
    }

    public SettingsStorage Setting() {
        if (_userName.isEmpty())
            throw new InvalidParameterException("No username set");

        if (_settingsStorage == null) {
            _settingsStorage = new SettingsStorage(_userName);
        }

        return _settingsStorage;
    }

    public void SetCurrentUser(String value) {
        _userName = value;
    }
}
