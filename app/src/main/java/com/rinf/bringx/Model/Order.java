package com.rinf.bringx.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Order {

    public Order(JSONObject parser) throws JSONException {
        _jsonObj = parser;

        // Validates the received object
        if (Id() == null || Version() == null)
            throw new JSONException("No Id or Version found");

        PriceGoods();
        if (PriceDelivery() == null) {
            throw new JSONException("Price of delivery not found");
        }

        PriceComment();

        if (NumberGoods() == null)
            throw new JSONException("Pickup or Delivery not found");

        if (PickupAddress() == null || DeliveryAddress() == null)
            throw new JSONException("Pickup or Delivery not found");

        if (Cargo() == null)
            throw new JSONException("Cargo not found");
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
            return _jsonObj.getString("order_uid").split("-")[0];
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String Version() {
        try {
            return _jsonObj.getString("order_uid").split("-")[1];
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Double PriceGoods() {
        return _jsonObj.optDouble("price_goods");
    }

    public Double PriceDelivery() {
        try {
            return _jsonObj.getDouble("price_delivery");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String PriceComment() {
        return _jsonObj.optString("price_comment");
    }

    public Integer NumberGoods() {
        try {
            return _jsonObj.getInt("item_type_count");
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
            pickupAddressObj = _jsonObj.getJSONObject("pickup_address");
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
            deliveryAddressObj = _jsonObj.getJSONObject("delivery_address");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        _deliveryAddress = new Address(deliveryAddressObj);

        return _deliveryAddress;
    }

    public List<Cargo> Cargo() {
        if (_cargo != null)
            return _cargo;

        JSONArray cargo = null;

        try {
            cargo = _jsonObj.getJSONArray("cargo");

            for (int i = 0; i < cargo.length(); i++) {
                if (_cargo == null)
                    _cargo = new ArrayList<Cargo>();

                _cargo.add(new Cargo(cargo.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return _cargo;
    }
}