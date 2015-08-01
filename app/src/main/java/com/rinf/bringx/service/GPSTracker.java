package com.rinf.bringx.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.rinf.bringx.Views.LoginActivity;
import com.rinf.bringx.R;
import com.rinf.bringx.utils.Log;
import com.rinf.bringx.utils.ServiceProxy;

public class GPSTracker extends Service implements LocationListener {

    public static int GPS_TRACKER_NOTIFICATION_ID = 11042015;

    boolean isServiceStart = true;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    boolean userDisabledGPS = false;
    boolean userDisabledNetwork = false;

    // User for setting the context when used from LoginActivity
    private Context mContext = null;
 
    Location location;
    double latitude = 0;
    double longitude = 0;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 20 * 1; // 20 seconds
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    private static String _uid = "";
    private static String _mobileId = "";
    private static String _oCount = "0";

    private ServiceProxy _sp = new ServiceProxy(null);

    protected LocationManager locationManager;

    public GPSTracker(Context ctx) {
        mContext = ctx;
    }

    public GPSTracker() {
        mContext = this;
    }

    private void registerLocationUpdateFromProvider(String provider) {
        if (locationManager == null) {
            Log.e("No location manager.");
            return;
        }

        locationManager.requestLocationUpdates(provider,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this);

        Log.d("Location provider" + provider);

        location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
    }

    public void CheckLocationProviders() {
        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean IsGPSEnabled() { return isGPSEnabled; }
    public boolean IsNetworkEnabled() { return isNetworkEnabled; }

    private boolean startLocationUpdates() {
        try
        {
            CheckLocationProviders();

            registerLocationUpdateFromProvider(LocationManager.NETWORK_PROVIDER);
            registerLocationUpdateFromProvider(LocationManager.GPS_PROVIDER);

            // Make first update
            onLocationUpdated(longitude, latitude);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
 
        return true;
    }
     

    private void stopLocationUpdates(){
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }      
    }

    private double getLatitude(){
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    private double getLongitude(){
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        if (newLocation == null)
            return;

        Log.e("location changed to: " + newLocation.getLongitude());
        onLocationUpdated(newLocation.getLongitude(), newLocation.getLatitude());
    }
 
    @Override
    public void onProviderDisabled(String provider) {
        Log.e(provider + " was disabled.");

        if (provider.equals(LocationManager.GPS_PROVIDER))
            userDisabledGPS = true;

        if (provider.equals(LocationManager.NETWORK_PROVIDER))
            userDisabledNetwork = true;

        if (userDisabledGPS && userDisabledNetwork) {
            Log.d("Both location providers disabled. Setting (0, 0).");
            onLocationUpdated(0, 0);
        }
    }
 
    @Override
    public void onProviderEnabled(String provider) {
        Log.e(provider + " was enabled.");

        if (provider.equals(LocationManager.GPS_PROVIDER))
            userDisabledGPS = false;

        if (provider.equals(LocationManager.NETWORK_PROVIDER))
            userDisabledNetwork = false;
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    // Service Start Stop Interface
    @Override
    public void onCreate() {
        startLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();

        if (extras == null) {
            Log.e("GPS Service: No parameters sent");
        }
        else
        {
            _oCount = extras.getString("ordersCount");

            if (_oCount != null) {
                onLocationUpdated(getLongitude(), getLatitude());
            } else {
                _uid = extras.getString("uid");
                _mobileId = (String) extras.getString("mobileid");
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // we don't want to bind
    }

    @Override
    public void onDestroy() {
        Log.d("Destroying GPS service.");

        stopForeground(true);
        stopLocationUpdates();
    }

    private void onLocationUpdated(double longitude, double latitude) {
        Log.d(String.valueOf(longitude) + ' ' + String.valueOf(latitude));

        Notification notification = new Notification.Builder(this)
                .setContentTitle(_oCount + " orders in queue")
                .setContentText("(" + String.valueOf(longitude) + ", " + String.valueOf(latitude) + ")")
                .setSmallIcon(R.mipmap.ic_icon_small)
                .build();

        Intent notificationIntent = new Intent(this, LoginActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.contentIntent = pendingIntent;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(GPS_TRACKER_NOTIFICATION_ID, notification);

        if (isServiceStart == true) {
            startForeground(GPS_TRACKER_NOTIFICATION_ID, notification);
            isServiceStart = false;
        }

        if (!_uid.isEmpty())
            _sp.UpdatePosition(latitude, longitude, _uid, System.currentTimeMillis() / 1000L);
    }
}