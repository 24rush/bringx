package com.rinf.bringx.Model;


import org.json.JSONException;
import org.json.JSONObject;

public class Address {
    private String _name;
    private String _company;
    private String _street;
    private String _zip;
    private String _instructions;
    private String _notes;
    private String _phone;
    private String _mail;
    private String _status;

    private Double _latitude = -1.;
    private Double _longitude = -1.;

    private JSONObject _jsonObj;

    public Address(JSONObject parser) {
        _jsonObj = parser;

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

            if (coord != null && !coord.equals("")) {
                String[] values = coord.split(",");
                _latitude = Double.parseDouble(values[0].trim());
                _longitude = Double.parseDouble(values[1].trim());
            }

            _status = parser.optString("DrpStatus");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String Name() {
        return _name;
    }

    public String Company() {
        return _company;
    }

    public String Street() {
        return _street;
    }

    public String Zip() {
        return _zip;
    }

    public String Instructions() {
        return _instructions;
    }

    public String Notes() {
        return _notes;
    }

    public String Phone() {
        return _phone;
    }

    public String Mail() {
        return _mail;
    }

    public Double Latitude() {
        return _latitude;
    }

    public Double Longitude() {
        return _longitude;
    }

    public boolean HasCoordinates() {
        return _latitude != -1. && _longitude != -1.;
    }

    public String Status() {
        return _status;
    }

    public Address Status(String status) {
        _status = status;

        try {
            _jsonObj.put("DrpStatus", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }
}