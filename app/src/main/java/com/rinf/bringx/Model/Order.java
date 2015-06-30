package com.rinf.bringx.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {

    public Order(JSONObject parser) throws JSONException {
        _jsonObj = parser;

        // Validates the received object
        if (Id() == null || Version() == null)
            throw new JSONException("No Id or Version found");

        if (PriceGoodsDelivery() == null ||
            PriceVatGoodsDelivery() == null ||
            PriceTransportDelivery() == null ||
            PriceVatTransportDelivery() == null ||

            PriceGoodsPickup() == null ||
            PriceVatGoodsPickup() == null ||
            PriceTransportPickup() == null ||
            PriceVatTransportPickup() == null) {
            throw new JSONException("Some prices of pickup/delivery were not found");
        }

        if (NumberGoods() == null)
            throw new JSONException("Pickup or Delivery not found");

        if (PickupAddress() == null || DeliveryAddress() == null)
            throw new JSONException("Pickup or Delivery not found");

        if (Cargo() == null)
            throw new JSONException("Cargo not found");

        Cta();
    }

    private JSONObject _jsonObj;

    private Address _pickupAddress;
    private Address _deliveryAddress;
    private List<Cargo> _cargo;

    private Date _ctaDeliveryTime;
    private Date _ctaPickupTime;

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

    private Double getFieldDouble(String name) {
        try {
            return _jsonObj.getDouble(name);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Double PriceGoodsPickup() {
        return getFieldDouble("price_goods_pickup");
    }

    public Double PriceVatGoodsPickup() {
        return getFieldDouble("price_vat_goods_pickup");
    }

    public Double PriceTransportPickup() {
        return getFieldDouble("price_transport_pickup");
    }

    public Double PriceVatTransportPickup() {
        return getFieldDouble("price_vat_transport_pickup");
    }

    public Double PriceGoodsDelivery() {
        return getFieldDouble("price_goods_delivery");
    }

    public Double PriceVatGoodsDelivery() {
        return getFieldDouble("price_vat_goods_delivery");
    }

    public Double PriceTransportDelivery() {
        return getFieldDouble("price_transport_delivery");
    }

    public Double PriceVatTransportDelivery() {
        return getFieldDouble("price_vat_transport_delivery");
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

    public void Cta() {
        String ctaStr = _jsonObj.optString("communicated_delivery_time");
        if (ctaStr != null && !ctaStr.isEmpty()) {
            _ctaDeliveryTime = new Date(Long.parseLong(ctaStr) * 1000);
        }

        ctaStr = _jsonObj.optString("communicated_pickup_time");
        if (ctaStr != null && !ctaStr.isEmpty()) {
            _ctaPickupTime = new Date(Long.parseLong(ctaStr) * 1000);
        }
    }

    public Date CtaDeliveryTime() { return _ctaDeliveryTime; }
    public Date CtaPickupTime() { return _ctaPickupTime; }
}