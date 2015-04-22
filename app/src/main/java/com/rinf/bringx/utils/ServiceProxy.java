package com.rinf.bringx.utils;

import android.os.AsyncTask;

import com.rinf.bringx.App;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.ViewModels.MEETING_STATUS;

import org.json.JSONArray;
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

    public void SetMeetingStatus(String userName, String orderId, MEETING_STATUS status) {
        MeetingStatusTask meetingStatusTask = new MeetingStatusTask(null);
        meetingStatusTask.execute(userName, orderId, status);
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
        List<Meeting> meetingList = (List<Meeting>) params[1];

        // Determine which orders need to be retrieved
        List<String> orderIdsToRetrive = new ArrayList<String>();
        Map<String, Order> ordersInCache = App.StorageManager().Orders().getAll();

        for (Meeting meeting : meetingList) {
            Order cachedOrder = ordersInCache.get(meeting.OrderID);
            if (cachedOrder == null || !cachedOrder.Version().equals(meeting.OrderVersion)) {
                orderIdsToRetrive.add(meeting.OrderID);
            }
        }

        // Make request to retrieve orders
        List<Order> newOrders = new ArrayList<Order>();
        String response = "[\n" +
                "{\"OrderUID\": \"1015-01\",\n" +
                "\"Price_goods\": 12.34, \n" +
                "\"Price_delivery\": 3.98,\n" +
                "\"Price_comment\": \"paid by credit card\", \n" +
                "\"number_goods\": 5,\n" +
                "\n" +
                "\"Pickup-Address\": {\n" +
                "\t\"Name\": \"Gaststätte Wohnzimmer\",\n" +
                "\t\"Company\": \"\", \n" +
                "\t\"Street\": \"Schloßstr. 77b\", \n" +
                "\t\"ZIP\": \"70176\",\n" +
                "\t\"Instructions\": \"Please call when arriving, the bell is broken\",\n" +
                "\t\"Notes\": \"Cheeseburger without Tomatoes please\",\n" +
                "\t\"Phone\": \"+49 175 5234632\",\n" +
                "\t\"Mail\": \"\"\n" +
                "},\n" +
                "\n" +
                "\"Delivery-Address\": {\n" +
                "\t\"Name\": \"Matthias Brunner\",\n" +
                "\t\"Company\": \"Logistics Start-up\",\n" +
                "\t\"Street\": \"Böblinger Str. 43\",\n" +
                "\t\"ZIP\": \"70196\",\n" +
                "\t\"Instructions\": \"\"\t,\n" +
                "\t\"Notes\":\"\",\n" +
                "\t\"Phone\": \"0176 8046 8925\",\n" +
                "\t\"Mail\": \"mbrunner@bringx.com\",\n" +
                "\t\"Coordinates\": \"42.94321, 9.813242\"\n" +
                "},\n" +
                "\n" +
                "\"Cargo\": \n" +
                "[\n" +
                "\t{\n" +
                "\t\"count\": 6,\n" +
                "\t\"price\": 5.35,\n" +
                "\t\"title\": \"Beck beer\",\t\n" +
                "\t\"size\":  \"\",\n" +
                "\t\"weight\": \"\", \n" +
                "\t\"info\": \"\"\n" +
                "\t},\n" +
                "\t{\t\n" +
                "\t\"count\": 1,\n" +
                "\t\"price\": 12.56,\n" +
                "\t\"title\": \"spare ribs with french fries\"\n" +
                "\t}\t\n" +
                "]\n" +
                "},\n" +
                "{\"OrderUID\": \"1014-01\",\n" +
                "\"Price_goods\": 12.34, \n" +
                "\"Price_delivery\": 3.98,\n" +
                "\"Price_comment\": \"paid by credit card\",\n" +
                "\"number_goods\": 5,   \n" +
                "\n" +
                "\"Pickup-Address\": {\n" +
                "\t\"Name\": \"Gaststätte Wohnzimmer\",\n" +
                "\t\"Company\": \"\", \n" +
                "\t\"Street\": \"Schloßstr. 77b\", \n" +
                "\t\"ZIP\": \"70176\",\n" +
                "\t\"Instructions\": \"Please call when arriving, the bell is broken\",\n" +
                "\t\"Notes\": \"Cheeseburger without Tomatoes please\",\n" +
                "\t\"Phone\": \"+49 175 5234632\",\n" +
                "\t\"Mail\": \"\"\n" +
                "},\n" +
                "\n" +
                "\"Delivery-Address\": {\n" +
                "\t\"Name\": \"Matthias Brunner\",\n" +
                "\t\"Company\": \"Logistics Start-up\",\n" +
                "\t\"Street\": \"Böblinger Str. 43\",\n" +
                "\t\"ZIP\": \"70196\",\n" +
                "\t\"Instructions\": \"\",\n" +
                "\t\"Notes\":\"\",\n" +
                "\t\"Phone\": \"0176 8046 8925\",\n" +
                "\t\"Mail\": \"mbrunner@bringx.com\",\n" +
                "\t\"Coordinates\": \"42.94321, 9.813242\"\n" +
                "},\n" +
                "\n" +
                "\"Cargo\": \n" +
                "[\n" +
                "\t{\n" +
                "\t\"count\": 6,\n" +
                "\t\"price\": 5.35,\n" +
                "\t\"title\": \"Beck beer\"\t,\n" +
                "\t\"size\":  \"\",\n" +
                "\t\"weight\": \"\", \n" +
                "\t\"info\": \"\"\n" +
                "\t},\n" +
                "\t{\t\n" +
                "\t\"count\": 1,\n" +
                "\t\"price\": 12.56,\n" +
                "\t\"title\": \"spare ribs with french fries\"\n" +
                "\t}\t\n" +
                "]\n" +
                "}]\n" +
                "\n";
        try {
            JSONArray resp = new JSONArray(response);
            for (int i = 0; i < resp.length(); i++) {
                Order newOrder = new Order(resp.getJSONObject(i));
                newOrder.DeliveryAddress().Status("pending");
                newOrders.add(newOrder);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            ReportError(500, e.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            ReportError(500, e.getLocalizedMessage());
            return null;
        }

        // Save new Orders
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

            meetingsListPayload = "1015-01,1429172461,1429172471,1014-01,,1429172961";

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


class MeetingStatusTask extends AsyncTaskReport<Object, Void, Boolean> {

    public MeetingStatusTask(IStatusHandler statusHandler) {
        super(statusHandler);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (params.length != 3) {
            return false;
        }

        JSONObject jsonObj = null;

        Log.d("Performing status update to " + params[2] + " for order: " + params[1] + " on device: " + App.DeviceManager().DeviceId());

        try {
            JSONObject jsonParams = new JSONObject();
            jsonObj = new JSONObject(App.Requester().POST(URLS.StatusURL, jsonParams));

            Thread.sleep(2000, 0);
            return true;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean jsonObj) {
        if (jsonObj == null) {
            ReportError(500, "Server error");
            return;
        }

        if (jsonObj == true) {
            ReportSuccess(true);
        } else {
            ReportError(1, "Invalid login credentials");
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
