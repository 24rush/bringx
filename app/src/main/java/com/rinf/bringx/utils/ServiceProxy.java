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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServiceProxy {
    private IStatusHandler _statusHandler;

    public ServiceProxy(IStatusHandler statusHandler) {
        _statusHandler = statusHandler;
    }

    private Boolean checkConnection() {
        return App.DeviceManager().IsNetworkAvailable();
    }

    public void Login(String userName, String password) {
        if (checkConnection() == false) {
            if (_statusHandler != null) {
                String[] params = { userName, password };
                _statusHandler.OnError(new Error(0, "No Internet Connection"), params);
            }

            return;
        }

        UserLoginTask loginTask = new UserLoginTask(_statusHandler, userName, password);
        loginTask.execute();
    }

    public void Logout(String driverId, String authToken) {
        if (checkConnection() == false) {
            if (_statusHandler != null) {
                String[] params = { driverId, authToken };
                _statusHandler.OnError(new Error(0, "No Internet Connection"), params);
            }

            return;
        }

        UserLogoutTask logoutTask = new UserLogoutTask(_statusHandler, driverId, authToken);
        logoutTask.execute();
    }

    public void GetMeetingsList(String userName, String driverId, String authToken) {
        MeetingsListTask meetingsListTask = new MeetingsListTask(_statusHandler, userName, driverId, authToken);
        meetingsListTask.execute();
    }

    public void GetOrdersList(String userName, List<Meeting> meetingsList, String driverId, String authToken) {
        OrdersListTask ordersListTask = new OrdersListTask(_statusHandler, userName, meetingsList, driverId, authToken);
        ordersListTask.execute();
    }

    public void UpdatePosition(Double latitude, Double longitude, String uid, Long ts) {
        UpdatePositionTask updatePositionTask = new UpdatePositionTask(_statusHandler, latitude, longitude, uid, ts);
        updatePositionTask.execute();
    }

    public void SetMeetingStatus(String orderId, String status, String rejectedReason, String driverId, String authToken) {
        if (checkConnection() == false) {
            if (_statusHandler != null)
                _statusHandler.OnError(new Error(0, "No Internet Connection"), orderId, status, rejectedReason, driverId, authToken);

            return;
        }

        MeetingStatusTask meetingStatusTask = new MeetingStatusTask(_statusHandler, orderId, status, rejectedReason, driverId, authToken);
        meetingStatusTask.execute();
    }
}

abstract class AsyncTaskReport<Params, Progress, Return> extends AsyncTask<Params, Progress, Return> {
    protected IStatusHandler _statusHandler;
    protected Params[] _params;

    public AsyncTaskReport(IStatusHandler statusHandler, Params... params) {
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

        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("hidden", "0");
            jsonParams.put("mobileid", App.DeviceManager().DeviceId());
            jsonParams.put("username", _params[0]);
            jsonParams.put("password", DataUtils.md5(_params[1]));

            String regId = App.StorageManager().Setting().getString(SettingsStorage.REG_ID);
            if (regId == null || regId.isEmpty())
                regId = "0";

            jsonParams.put("registration_id", regId);

            Log.d("Performing login for: " + _params[0] + " on device: " + App.DeviceManager().DeviceId() + " with registration_id: " + regId);

            return new JSONObject(App.Requester().POST(URLS.LoginURL, jsonParams));

        } catch (JSONException e) {
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
            if (!jsonObj.getString("status").equals("fail")) {
                JSONObject data = jsonObj.getJSONObject("data");

                if (data != null && !data.optString("uid").isEmpty() && !data.optString("auth_token").isEmpty()) {
                    ReportSuccess(data);
                } else {
                    ReportError(500, "Invalid login response");
                }

            } else {
                ReportError(403, "Invalid login credentials");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class UserLogoutTask extends AsyncTaskReport<String, Void, JSONObject> {

    public UserLogoutTask(IStatusHandler statusHandler, String... params) {
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

        Log.d("Performing logout for: " + _params[0] + " on device: " + App.DeviceManager().DeviceId());

        try {

            String url = String.format(URLS.LogoutURL, _params[0], _params[1]);
            String response = App.Requester().POST(url, null);

            // Force logout in case it fails (403 codes)
            if (response == null || response.isEmpty()) {
                jsonObj = new JSONObject();
                jsonObj.put("status", "true");
            }
            else {
                jsonObj = new JSONObject(response);
            }

        } catch (JSONException e) {
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
            if (!jsonObj.getString("status").equals("true")) {
                JSONObject data = jsonObj.getJSONObject("data");

                if (data != null) {
                    ReportError(Integer.parseInt(data.optString("code")), data.optString("message"));
                }

            } else {
                ReportSuccess(jsonObj);
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
        if (_params.length < 4)
            return null;

        String userName = (String) _params[0];
        List<Meeting> meetingList = (List<Meeting>) _params[1];

        // Orders already in cache
        List<Order> existingOrders = new LinkedList<Order>();

        // Determine which orders need to be retrieved
        String orderIdsToRetrieve = "";
        Map<String, Order> ordersInCache = App.StorageManager().Orders().getAll();

        for (String orderId : ordersInCache.keySet()) {
            Boolean meetingRequired = false;
            for (Meeting meeting : meetingList) {
                if (orderId.equals(meeting.OrderID)) {
                    meetingRequired = true;
                    break;
                }
            }

            if (!meetingRequired) {
                App.StorageManager().Orders().remove(orderId);
            }
        }

        for (Meeting meeting : meetingList) {
            Order cachedOrder = ordersInCache.get(meeting.OrderID);

            if (cachedOrder == null || !cachedOrder.Version().equals(meeting.OrderVersion)) {
                orderIdsToRetrieve += meeting.OrderID + "-" + meeting.OrderVersion + "--";
            } else {
                existingOrders.add(cachedOrder);
            }
        }

        // Make request to retrieve orders
        List<Order> newOrders = new LinkedList<Order>();

        if (!orderIdsToRetrieve.isEmpty()) {
            orderIdsToRetrieve = orderIdsToRetrieve.substring(0, orderIdsToRetrieve.length() - 2);

            try {
                String url = String.format(URLS.OrdersInfo, _params[2], orderIdsToRetrieve, _params[3]);
                String response = App.Requester().GET(url, null);
                Log.d("Orders: " + response);
                if (response.contains("status")) {
                    ReportError(500, "Failed to get orders list");
                    return null;
                }

                JSONArray resp = new JSONArray(response);
                for (int i = 0; i < resp.length(); i++) {
                    try {
                        Order newOrder = new Order(resp.getJSONObject(i));
                        newOrder.DeliveryAddress().Status("pending");
                        newOrder.PickupAddress().Status("pending");
                        newOrders.add(newOrder);
                    }
                    catch (JSONException e) {
                        Log.e("Error parsing order");
                        e.printStackTrace();
                    }
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

                existingOrders.add(newOrder);
            }
        }

        return existingOrders;
    }

    @Override
    protected void onPostExecute(List<Order> ordersList) {
        if (ordersList == null) {
            ReportError(500, "Server error");
            return;
        }

        ReportSuccess(ordersList);
    }
}

class MeetingsListTask extends AsyncTaskReport<String, Void, List<Meeting>> {

    public MeetingsListTask(IStatusHandler statusHandler, String... p) {
        super(statusHandler, p);
    }

    public List<Meeting> OnMeetingsListReceived(String meetingsListPayload) {
        List<Meeting> meetingList = new ArrayList<Meeting>();

        if (meetingsListPayload == null || meetingsListPayload.isEmpty())
            return meetingList;

        // In case we get JSON response
        meetingsListPayload = meetingsListPayload.replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\"", "");

        String[] tokens = meetingsListPayload.split(",");

        if (meetingsListPayload.isEmpty())
            return meetingList;

        for (int i = 0; i < tokens.length; i += 3) {
            String orderCompStr = tokens[i].trim();
            String etaPickupStr = tokens[i + 1].trim();
            String etaDeliveryStr = tokens[i + 2].trim();

            String[] arrOrderComp = orderCompStr.split("-");
            String orderId = arrOrderComp[0];
            String orderVersion = arrOrderComp[1];

            Date etaPickup = null;
            if (!etaPickupStr.equals("") && !etaPickupStr.equals("false")) {
                etaPickup = new Date(Long.parseLong(etaPickupStr) * 1000);
            }

            Date etaDelivery = null;
            if (!etaDeliveryStr.equals("") && !etaDeliveryStr.equals("false")) {
                etaDelivery = new Date(Long.parseLong(etaDeliveryStr) * 1000);
            }

            Meeting meeting = new Meeting(orderId, orderVersion, etaPickup, etaDelivery);
            meetingList.add(meeting);
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
    protected List<Meeting> doInBackground(String... a) {
        if (_params.length != 3) {
            return null;
        }

        String meetingsListPayload = "";

        Log.d("Performing GetMeetingsList for: " + _params[0] + " on device: " + App.DeviceManager().DeviceId() + " uid:" + _params[1] + " auth:" + _params[2]);

        if (App.DeviceManager().IsNetworkAvailable() == false) {
            meetingsListPayload = App.StorageManager().Setting().getString(SettingsStorage.LAST_MEETINGS_ORDER);
        } else {
            String url = String.format(URLS.OrdersETA, _params[1], _params[2]);

            meetingsListPayload = App.Requester().GET(url, null);

            // If we get a formatted JSON response with status code then fail
            if (meetingsListPayload == null || meetingsListPayload.contains("status")) {
                ReportError(500, "Failed to get meetings list");
            }
        }

        return OnMeetingsListReceived(meetingsListPayload);
    }

    @Override
    protected void onPostExecute(List<Meeting> meetingsList) {
        if (meetingsList == null) {
            ReportError(500, "Server error");
            return;
        }

        ReportSuccess(meetingsList);
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

    //String orderId, String status, String rejectedReason, String driverId, String authToken

    @Override
    protected Boolean doInBackground(String... a) {
        if (_params.length != 5) {
            return false;
        }

        JSONObject jsonObj = null;

        Log.d("Performing status update to " + _params[1] + " for order: " + _params[0] + " on device: " + App.DeviceManager().DeviceId());

        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("status", _params[1]);
            jsonParams.put("status_notes", _params[2].length() > 255 ? _params[2].substring(0, 254) : _params[2]);

            //"http://dev-auftrag.bringx.com//json/drivers/%s/orders/%s/status?auth_token=%s&version=1.0.1";
            String url = String.format(URLS.StatusURL, _params[3], _params[0], _params[4]);
            jsonObj = new JSONObject(App.Requester().PUT(url, jsonParams));

            if (jsonObj.optString("status").equals("success"))
                return true;

            return false;

        } catch (JSONException e) {
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

class UpdatePositionTask extends AsyncTaskReport<Object, Void, Boolean> {

    public UpdatePositionTask(IStatusHandler statusHandler, Object... p) {
        super(statusHandler, p);
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        if (_params.length != 4) {
            return false;
        }

        JSONObject jsonObj = null;

        Log.d("Performing position update (" + _params[0] + "," + _params[1] + ") for uid: " + _params[2] + " on device: " + App.DeviceManager().DeviceId());

        try {
            JSONObject jsonParams = new JSONObject();

            // Reversed lat/long
            jsonParams.put("latitude", (double) (_params[1]) * 1.0);
            jsonParams.put("longitude", (double) (_params[0]) * 1.0);

            jsonParams.put("uid", (String) _params[2]);
            jsonParams.put("mobileid", (String) App.DeviceManager().DeviceId());
            jsonParams.put("ts_pos", (long) _params[3]);

            String url = String.format(URLS.PositionUpdateURL, _params[2]);
            jsonObj = new JSONObject(App.Requester().POST(url, jsonParams));

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result == true) {
            ReportSuccess(true);
        } else {
            ReportError(500, "Position update failed");
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
