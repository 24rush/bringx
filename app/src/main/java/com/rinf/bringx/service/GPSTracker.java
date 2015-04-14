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
import android.util.Log;

import com.rinf.bringx.Views.LoginActivity;
import com.rinf.bringx.R;

public class GPSTracker extends Service implements LocationListener {

    int GPS_TRACKER_NOTIFICATION_ID = 11042015;

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

    protected LocationManager locationManager;

    public GPSTracker(Context ctx) {
        mContext = ctx;
    }

    public GPSTracker() {
        mContext = this;
    }

    private void registerLocationUpdateFromProvider(String provider) {
        if (locationManager == null) {
            Log.e("[bringx]", "No location manager.");
            return;
        }

        locationManager.requestLocationUpdates(provider,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this);

        Log.d("Location provider", provider);

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

        Log.e("[bringx]", "location changed to: " + newLocation.getLongitude());
        onLocationUpdated(newLocation.getLongitude(), newLocation.getLatitude());
    }
 
    @Override
    public void onProviderDisabled(String provider) {
        Log.e("[bringx]", provider + " was disabled.");

        if (provider.equals(LocationManager.GPS_PROVIDER))
            userDisabledGPS = true;

        if (provider.equals(LocationManager.NETWORK_PROVIDER))
            userDisabledNetwork = true;

        if (userDisabledGPS && userDisabledNetwork) {
            Log.d("[bringx]", "Both location providers disabled. Setting (0, 0).");
            onLocationUpdated(0, 0);
        }
    }
 
    @Override
    public void onProviderEnabled(String provider) {
        Log.e("[bringx]", provider + " was enabled.");

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
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // we don't want to bind
    }

    @Override
    public void onDestroy() {
        Log.d("[bringx]", "Destroying GPS service.");

        stopForeground(true);
        stopLocationUpdates();
    }

    private void onLocationUpdated(double longitude, double latitude) {
        Log.d("[bringx]", String.valueOf(longitude) + ' ' + String.valueOf(latitude));

        Notification notification = new Notification.Builder(this)
                .setContentTitle("BringX GPS Tracker")
                .setContentText("(" + String.valueOf(longitude) + ", " + String.valueOf(latitude) + ")")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.contentIntent = pendingIntent;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(GPS_TRACKER_NOTIFICATION_ID, notification);

        if (isServiceStart == true) {
            startForeground(GPS_TRACKER_NOTIFICATION_ID, notification);
            isServiceStart = false;
        }
    }
}