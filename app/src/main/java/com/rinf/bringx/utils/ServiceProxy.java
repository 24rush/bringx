package com.rinf.bringx.utils;

import android.os.AsyncTask;

import com.rinf.bringx.App;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.ViewModels.MEETING_STATUS;
import com.rinf.bringx.storage.SettingsStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private Boolean checkConnection() {
        return App.DeviceManager().IsNetworkAvailable();
    }

    public void Login(String userName, String password) {
        UserLoginTask loginTask = new UserLoginTask(_statusHandler, userName, password);
        loginTask.execute();
    }

    public void GetMeetingsList(String userName) {
        MeetingsListTask meetingsListTask = new MeetingsListTask(_statusHandler, userName);
        meetingsListTask.execute();
    }

    public void GetOrdersList(String userName, List<Meeting> meetingsList) {
        OrdersListTask ordersListTask = new OrdersListTask(_statusHandler, userName, meetingsList);
        ordersListTask.execute();
    }

    public void SetMeetingStatus(String userName, String orderId, String status) {
        if (checkConnection() == false) {
            if (_statusHandler != null)
                _statusHandler.OnError(new Error(0, "No Internet Connection"), userName, orderId, status);

            return;
        }

        MeetingStatusTask meetingStatusTask = new MeetingStatusTask(_statusHandler, userName, orderId, status);
        meetingStatusTask.execute();
    }
}

abstract class AsyncTaskReport<Params, Progress, Return> extends AsyncTask<Params, Progress, Return> {
    protected IStatusHandler _statusHandler;
    protected Params[] _params;

    public AsyncTaskReport(IStatusHandler statusHandler, Params ...params) {
        _statusHandler = statusHandler;
        _params = params.clone();
    }

    protected void ReportError(int code, String message) {
        if (_statusHandler != null) {
            _statusHandler.OnError(new Error(code, message), _params);
        }
    }

    protected void ReportSuccess(Return response) {
        if (_statusHandler != null) {
            Log.d("calling success with " + _params);
            _statusHandler.OnSuccess(response, _params);
        }
    }
}

class UserLoginTask extends AsyncTaskReport<String, Void, JSONObject> {

    public UserLoginTask(IStatusHandler statusHandler, String... params) {
        super(statusHandler, params);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... a) {
        if (_params.length != 2) {
            return null;
        }

        JSONObject jsonObj = null;

        Log.d("Performing login for: " + _params[0] + " on device: " + App.DeviceManager().DeviceId());

        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("hidden", "0");
            jsonParams.put("mobileid", App.DeviceManager().DeviceId());
            jsonParams.put("username", _params[0]);
            jsonParams.put("password", DataUtils.md5(_params[1]));

            jsonObj = new JSONObject(App.Requester().POST(URLS.LoginURL, jsonParams));

            Thread.sleep(2000, 0);

            if (_params[0].equals("a") && _params[1].equals("b"))
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
                ReportError(403, "Invalid login credentials");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class OrdersListTask extends AsyncTaskReport<Object, Void, List<Order>> {

    public OrdersListTask(IStatusHandler statusHandler, Object... p) {
        super(statusHandler, p);
    }

    @Override
    protected List<Order> doInBackground(Object... a) {
        if (_params.length < 2)
            return null;

        String userName = (String) _params[0];
        List<Meeting> meetingList = (List<Meeting>) _params[1];

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
        String response = "[" +
                "{\"OrderUID\": \"1015-01\"," +
                "\"Price_goods\": 12.34, " +
                "\"Price_delivery\": 3.98," +
                "\"Price_comment\": \"paid by credit card\", " +
                "\"number_goods\": 5," +
                "\"Pickup-Address\": {" +
                "\"Name\": \"Gaststätte Wohnzimmer\"," +
                "\"Company\": \"\", " +
                "\"Street\": \"Schloßstr. 77b\", " +
                "\"ZIP\": \"70176\"," +
                "\"Instructions\": \"Please call when arriving, the bell is broken\"," +
                "\"Notes\": \"Cheeseburger without Tomatoes please\"," +
                "\"Phone\": \"+49 175 5234632\"," +
                "\"Mail\": \"\"," +
                "\"DrpStatus\": \"pending\"" +
                "}," +
                "" +
                "\"Delivery-Address\": {" +
                "\"Name\": \"Matthias Brunner\"," +
                "\"Company\": \"Logistics Start-up\"," +
                "\"Street\": \"Böblinger Str. 43\"," +
                "\"ZIP\": \"70196\"," +
                "\"Instructions\": \"\"," +
                "\"Notes\":\"\"," +
                "\"Phone\": \"0176 8046 8925\"," +
                "\"Mail\": \"mbrunner@bringx.com\"," +
                "\"DrpStatus\": \"pending\"," +
                "\"Coordinates\": \"42.94321, 9.813242\"" +
                "}," +
                "\"Cargo\": " +
                "[" +
                "{" +
                "\"count\": 6," +
                "\"price\": 5.35," +
                "\"title\": \"Beck beer\"," +
                "\"size\":  \"\"," +
                "\"weight\": \"\", " +
                "\"info\": \"\"" +
                "}," +
                "{" +
                "\"count\": 1," +
                "\"price\": 12.56," +
                "\"title\": \"spare ribs with french fries\"" +
                "}" +
                "]" +
                "}," +
                "{\"OrderUID\": \"1014-01\"," +
                "\"Price_goods\": 12.34, " +
                "\"Price_delivery\": 3.98," +
                "\"Price_comment\": \"paid by credit card\"," +
                "\"number_goods\": 5,   " +
                "" +
                "\"Pickup-Address\": {" +
                "\"Name\": \"Gaststätte Wohnzimmer\"," +
                "\"Company\": \"\", " +
                "\"Street\": \"Schloßstr. 77b\", " +
                "\"ZIP\": \"70176\"," +
                "\"Instructions\": \"Please call when arriving, the bell is broken\"," +
                "\"Notes\": \"Cheeseburger without Tomatoes please\"," +
                "\"Phone\": \"+49 175 5234632\"," +
                "\"Mail\": \"\"," +
                "\"DrpStatus\": \"pending\"" +
                "}," +
                "" +
                "\"Delivery-Address\": {" +
                "\"Name\": \"Matthias Brunner\"," +
                "\"Company\": \"Logistics Start-up\"," +
                "\"Street\": \"Böblinger Str. 43\"," +
                "\"ZIP\": \"70196\"," +
                "\"Instructions\": \"\"," +
                "\"Notes\":\"\"," +
                "\"Phone\": \"0176 8046 8925\"," +
                "\"Mail\": \"mbrunner@bringx.com\"," +
                "\"Coordinates\": \"42.94321, 9.813242\"," +
                "\"DrpStatus\": \"pending\"" +
                "}," +
                "" +
                "\"Cargo\": " +
                "[" +
                "{" +
                "\"count\": 6," +
                "\"price\": 5.35," +
                "\"title\": \"Beck beer\"," +
                "\"size\":  \"\"," +
                "\"weight\": \"\", " +
                "\"info\": \"\"" +
                "}," +
                "{" +
                "\"count\": 1," +
                "\"price\": 12.56," +
                "\"title\": \"spare ribs with french fries\"" +
                "}" +
                "]" +
                "}]" +
                "";
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

    public MeetingsListTask(IStatusHandler statusHandler, String... p) {
        super(statusHandler, p);
    }

    @Override
    protected List<Meeting> doInBackground(String... a) {
        if (_params.length != 1) {
            return null;
        }

        List<Meeting> meetingList = null;

        Log.d("Performing GetMeetingsList for: " + _params[0] + " on device: " + App.DeviceManager().DeviceId());

        try {
            String meetingsListPayload = "";

            if (App.DeviceManager().IsNetworkAvailable() == false) {
                meetingsListPayload = App.StorageManager().Setting().getString(SettingsStorage.LAST_MEETINGS_ORDER);
            } else {
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("mobileid", App.DeviceManager().DeviceId());

                //meetingsListPayload = App.Requester().POST(URLS.JobsURL, jsonParams);
                Thread.sleep(2000, 0);
                meetingsListPayload = "1015-01,1429172461,1429172471,1014-01,,1429172961";
            }

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

        // Save meetings list
        String mList = "";
        for (Meeting meet : meetingList) {
             mList += meet.toString() + ",";
        }

        if (!mList.isEmpty()) {
            mList = mList.substring(0, mList.length() - 1);
            App.StorageManager().Setting().setString(SettingsStorage.LAST_MEETINGS_ORDER, mList);
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

class MeetingStatusTask extends AsyncTaskReport<String, Void, Boolean> {

    public MeetingStatusTask(IStatusHandler statusHandler, String... p) {
        super(statusHandler, p);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... a) {
        if (_params.length != 3) {
            return false;
        }

        JSONObject jsonObj = null;

        Log.d("Performing status update to " + _params[2] + " for order: " + _params[1] + " on device: " + App.DeviceManager().DeviceId());

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
            ReportError(500, "Status update failed");
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
