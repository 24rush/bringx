package com.rinf.bringx.Model;

import org.json.JSONException;
import org.json.JSONObject;


class Cargo {
    private int _count;
    private double _price;
    private String _title;
    private String _size;
    private String _weight;
    private String _info;

    public Cargo(JSONObject parser) {
        try {
            _count = parser.getInt("count");
            _price = parser.getDouble("price");
            _title = parser.getString("title");
            _size = parser.optString("size");
            _weight = parser.optString("weight");
            _info = parser.optString("info");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class Address {
    private String _name;
    private String _company;
    private String _street;
    private String _zip;
    private String _instructions;
    private String _notes;
    private String _phone;
    private String _mail;

    private Double _latitude;
    private Double _longitude;

    public Address(JSONObject parser) {
        try {
            _name = parser.getString("Name");
            _company = parser.optString("Company");
            _street = parser.getString("Street");
            _zip = parser.getString("ZIP");
            _instructions = parser.optString("Instructions");
            _notes = parser.optString("Notes");
            _phone = parser.getString("Phone");
            _mail = parser.optString("Mail");

            String coord = parser.optString("Coordinates");

            if (coord != null) {
                String[] values = coord.split(",");
                _latitude = Double.parseDouble(values[0].trim());
                _longitude = Double.parseDouble(values[1].trim());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

public class Order {

    public Order() {

    }

    public Order(JSONObject parser) {
        String idAndVersionStr = null;

        try {
            idAndVersionStr = parser.getString("OrderUID");
            String[] idAndVersion = idAndVersionStr.split("-");
            _id = idAndVersion[0];
            _version = idAndVersion[1];

            _priceGoods = parser.optDouble("Price_Goods");
            _priceDelivery = parser.getDouble("Price_Delivery");
            _priceComment = parser.optString("Price_comment");

            _numberGoods = parser.getInt("number_goods");

            JSONObject pickupAddressObj = parser.getJSONObject("Pickup-Address");
            _pickupAddress = new Address(pickupAddressObj);

            JSONObject deliveryAddressObj = parser.getJSONObject("Delivery-Address");
            _deliveryAddress = new Address(deliveryAddressObj);

            JSONObject cargo = parser.getJSONObject("Cargo");
            _cargo = new Cargo(cargo);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String _id;
    private String _version;
    private double _priceGoods;
    private double _priceDelivery;
    private String _priceComment;
    private int _numberGoods;

    private Address _pickupAddress;
    private Address _deliveryAddress;

    private Cargo _cargo;

    public String Id() {
        return _id;
    }

    public String Version() {
        return _version;
    }
}