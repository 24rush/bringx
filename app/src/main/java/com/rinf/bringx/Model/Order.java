package com.rinf.bringx.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Order {

    public Order(JSONObject parser) {
        _jsonObj = parser;

        try {

            // Validates the received object
            if (Id() == null || Version() == null)
                throw new JSONException("No Id or Version found");

            PriceGoods();
            PriceDelivery();
            PriceComment();

            if (NumberGoods() == null)
                throw new JSONException("Pickup or Delivery not found");

            if (PickupAddress() == null || DeliveryAddress() == null)
                throw new JSONException("Pickup or Delivery not found");

            Cargo();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject _jsonObj;

    private Address _pickupAddress;
    private Address _deliveryAddress;
    private List<Cargo> _cargo;

    @Override
    public String toString() {
        return _jsonObj.toString();
    }

    public String Id() {
        try {
            return _jsonObj.getString("OrderUID").split("-")[0];
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String Version() {
        try {
            return _jsonObj.getString("OrderUID").split("-")[1];
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double PriceGoods() {
        return _jsonObj.optDouble("Price_goods");
    }

    public double PriceDelivery() throws JSONException {
        return _jsonObj.getDouble("Price_delivery");
    }

    public String PriceComment() {
        return _jsonObj.optString("Price_comment");
    }

    public Integer NumberGoods() {
        try {
            return _jsonObj.getInt("number_goods");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Address PickupAddress() {
        if (_pickupAddress != null)
            return _pickupAddress;

        JSONObject pickupAddressObj = null;
        try {
            pickupAddressObj = _jsonObj.getJSONObject("Pickup-Address");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        _pickupAddress = new Address(pickupAddressObj);

        return _pickupAddress;
    }

    public Address DeliveryAddress() {
        if (_deliveryAddress != null)
            return _deliveryAddress;

        JSONObject deliveryAddressObj = null;
        try {
            deliveryAddressObj = _jsonObj.getJSONObject("Delivery-Address");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        _deliveryAddress = new Address(deliveryAddressObj);

        return _deliveryAddress;
    }

    public List<Cargo> Cargo() throws JSONException {
        if (_cargo != null)
            return _cargo;

        JSONArray cargo = _jsonObj.getJSONArray("Cargo");
        for (int i = 0; i < cargo.length(); i++) {
            if (_cargo == null)
                _cargo = new ArrayList<Cargo>();

            _cargo.add(new Cargo(cargo.getJSONObject(i)));
        }

        return _cargo;
    }
}