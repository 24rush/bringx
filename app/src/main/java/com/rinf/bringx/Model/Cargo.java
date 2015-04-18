package com.rinf.bringx.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class Cargo {
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

    public int Count() {
        return _count;
    }

    public double Price() {
        return _price;
    }

    public String Title() {
        return _title;
    }

    public String Size() {
        return _size;
    }

    public String Weight() {
        return _weight;
    }

    public String Info() {
        return _info;
    }
}

