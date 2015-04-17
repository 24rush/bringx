package com.rinf.bringx.utils;

import android.os.AsyncTask;

import com.rinf.bringx.App;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

class URLS {
    public static String LoginURL = "http://auftrag.bringx.com/json/login/1";
    public static String JobsURL = "http://auftrag.bringx.com/json/route/";
    public static String StatusURL = "http://auftrag.bringx.com/json/drp/";
}

public class ServiceProxy {
    private IStatusHandler _statusHandler;

    public ServiceProxy(IStatusHandler statusHandler) {
        _statusHandler = statusHandler;
    }

    public void Login(String userName, String password) {
        UserLoginTask loginTask = new UserLoginTask(_statusHandler);
        loginTask.execute(userName, password);
    }

    public void GetMeetingsList(String userName) {
        MeetingsListTask meetingsListTask = new MeetingsListTask(_statusHandler);
        meetingsListTask.execute(userName);
    }

    public void GetOrdersList(String userName, List<Meeting> meetingsList) {
        OrdersListTask ordersListTask = new OrdersListTask(_statusHandler);
        ordersListTask.execute(userName, meetingsList);
    }
}

abstract class AsyncTaskReport<Params, Progress, Return> extends AsyncTask<Params, Progress, Return> {
    protected IStatusHandler _statusHandler;

    public AsyncTaskReport(IStatusHandler statusHandler) {
        _statusHandler = statusHandler;
    }

    protected void ReportError(int code, String message) {
        if (_statusHandler != null) {
            _statusHandler.OnError(new Error(code, message));
        }
    }

    protected void ReportSuccess(Return response) {
        if (_statusHandler != null) {
            _statusHandler.OnSuccess(response);
        }
    }
}

class UserLoginTask extends AsyncTaskReport<String, Void, JSONObject> {

    public UserLoginTask(IStatusHandler statusHandler) {
        super(statusHandler);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if (params.length != 2) {
            return null;
        }

        JSONObject jsonObj = null;

        Log.d("Performing login for: " + params[0] + " on device: " + App.DeviceManager().DeviceId());

        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("hidden", "0");
            jsonParams.put("mobileid", App.DeviceManager().DeviceId());
            jsonParams.put("username", params[0]);
            jsonParams.put("password", DataUtils.md5(params[1]));

            jsonObj = new JSONObject(App.Requester().POST(URLS.LoginURL, jsonParams));

            Thread.sleep(2000, 0);

            if (params[0].equals("a") && params[1].equals("b"))
                jsonObj = new JSONObject("{\"status\":\"true\"}");
            else
                jsonObj = new JSONObject("{\"status\":\"false\"}");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObj) {
        if (jsonObj == null) {
            ReportError(500, "Server error");
            return;
        }

        try {
            if (jsonObj.getString("status").equals("true")) {
                ReportSuccess(jsonObj);
            } else {
                ReportError(1, "Invalid login credentials");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class OrdersListTask extends AsyncTaskReport<Object, Void, List<Order>> {

    public OrdersListTask(IStatusHandler statusHandler) {
        super(statusHandler);
    }

    @Override
    protected List<Order> doInBackground(Object... params) {
        if (params.length < 2)
            return null;

        String userName = (String) params[0];
        List<Meeting> meetingList = (List<Meeting>)params[1];

        // Determine which orders need to be retrieved
        List<String> orderIdsToRetrive = new ArrayList<String>();
        Map<String, ?> ordersInCache = App.StorageManager().Orders().getAll();

        for (Meeting meeting : meetingList) {
            Order cachedOrder = (Order) ordersInCache.get(meeting.OrderID);
            if (cachedOrder == null || !cachedOrder.Version().equals(meeting.OrderVersion)) {
                orderIdsToRetrive.add(meeting.OrderID);
            }
        }

        // Make request to retrieve orders
        List<Order> newOrders = new ArrayList<Order>();
        newOrders.add(new Order());
        newOrders.add(new Order());

        for (Order newOrder : newOrders) {
            App.StorageManager().Orders().setString(newOrder.Id(), newOrder.toString());
        }

        return newOrders;
    }

    @Override
    protected void onPostExecute(List<Order> ordersList) {
        if (ordersList == null) {
            ReportError(500, "Server error");
            return;
        }

        if (ordersList.size() > 0) {
            ReportSuccess(ordersList);
        } else {
            ReportError(1, "No orders");
        }
    }
}

class MeetingsListTask extends AsyncTaskReport<String, Void, List<Meeting>> {

    public MeetingsListTask(IStatusHandler statusHandler) {
        super(statusHandler);
    }

    @Override
    protected List<Meeting> doInBackground(String... params) {
        if (params.length != 1) {
            return null;
        }

        List<Meeting> meetingList = null;

        Log.d("Performing GetMeetingsList for: " + params[0] + " on device: " + App.DeviceManager().DeviceId());

        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("mobileid", App.DeviceManager().DeviceId());

            String meetingsListPayload = App.Requester().POST(URLS.JobsURL, jsonParams);
            Thread.sleep(2000, 0);

            meetingsListPayload = "1015-10,1429172461,1429172561,1014-11,,1429172461";

            String[] tokens = meetingsListPayload.split(",");

            meetingList = new ArrayList<Meeting>();

            for (int i = 0; i < tokens.length; i += 3) {
                String orderCompStr = tokens[i].trim();
                String etaPickupStr = tokens[i + 1].trim();
                String etaDeliveryStr = tokens[i + 2].trim();

                String[] arrOrderComp = orderCompStr.split("-");
                String orderId = arrOrderComp[0];
                String orderVersion = arrOrderComp[1];

                Date etaPickup = null;
                if (!etaPickupStr.equals("")) {
                    etaPickup = new Date(Long.parseLong(etaPickupStr) * 1000);
                }

                Date etaDelivery = null;
                if (!etaDeliveryStr.equals("")) {
                    etaDelivery = new Date(Long.parseLong(etaDeliveryStr) * 1000);
                }

                Meeting meeting = new Meeting(orderId, orderVersion, etaPickup, etaDelivery);
                meetingList.add(meeting);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return meetingList;
    }

    @Override
    protected void onPostExecute(List<Meeting> meetingsList) {
        if (meetingsList == null) {
            ReportError(500, "Server error");
            return;
        }

        if (meetingsList.size() > 0) {
            ReportSuccess(meetingsList);
        } else {
            ReportError(1, "No meetings");
        }
    }
}

class DataUtils {
    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
