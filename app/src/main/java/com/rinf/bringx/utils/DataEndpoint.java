package com.rinf.bringx.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DataEndpoint {

    private String _mockOrders =
            "[ {" +
                "\"orderid\" : \"M123\"" +
              "}, " +
              "{" +
                "\"orderid\" : \"M223\"" +
              "}]";

    public List<Order> GetOrders() {
        try
        {
            JSONArray ordersList = new JSONArray(_mockOrders);

            for (int i=0; i < ordersList.length(); i++) {
                JSONObject obj = ordersList.getJSONObject(i);
                Log.e("[bringx]", obj.toString());
            }

        } catch (JSONException e) {
            Log.e("[bringx]", e.toString());
            e.printStackTrace();
        }

        return null;
    }
}
