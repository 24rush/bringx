package com.rinf.bringx.Model;


import com.rinf.bringx.App;
import com.rinf.bringx.utils.Log;

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
            _name = parser.getString("name");
            _company = parser.optString("company");
            _street = parser.getString("street");
            _zip = parser.getString("zip");
            _instructions = parser.optString("instruction");
            _notes = parser.optString("notes");
            _phone = parser.getString("phone");
            _mail = parser.optString("mail");

            JSONObject coord = parser.optJSONObject("coordinates");

            if (coord != null) {
                _latitude = coord.getDouble("X");
                _longitude = coord.getDouble("Y");
            }

            _status = "";

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
            Log.d("update json to" + status);
            _jsonObj.put("DrpStatus", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }
}