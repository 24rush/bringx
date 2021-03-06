package com.rinf.bringx.utils;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rinf.bringx.App;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.ViewModels.VM;
import com.rinf.bringx.Views.LoginActivity;
import com.rinf.bringx.storage.SettingsStorage;

import java.util.List;

public class PushNotificationsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Received push notification");

        if (VM.LoginViewModel != null && VM.LoginViewModel.IsLoggedIn.get() == false) {
            Log.d("No user is logged in");
            return;
        }

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(App.Context());
        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();
        Log.d("Received: " + messageType + " extras: " + extras.toString() + "msg: " + extras.getString("message"));

        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            String newMeetingList = extras.getString("ml");
            String message = extras.getString("message");

            if (newMeetingList != null) {
                Log.d("Received new meeting list: " + newMeetingList);

                MeetingsListTask meetingsListTask = new MeetingsListTask(null);
                List<Meeting> parsedMeetings = meetingsListTask.OnMeetingsListReceived(newMeetingList);

                Log.d("App is visible: " + App.IsVisible());

                if (parsedMeetings != null) {
                    if (VM.MeetingsViewModel != null)
                        VM.MeetingsViewModel.OnPushReceived(parsedMeetings);

                    if (App.IsVisible() == false) {
                        App.StorageManager().Setting().setBoolean(SettingsStorage.FIST_MEETING_CHANGED, true);

                        final Intent notificationIntent = new Intent(App.Context(), LoginActivity.class);
                        notificationIntent.setAction(Intent.ACTION_MAIN);
                        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(notificationIntent);
                    }
                }
            }
            if (message != null) {
                Log.d("Received new message: " + message);

                if (App.IsVisible() == false) {
                    App.StorageManager().Setting().setString(SettingsStorage.MESSAGE_RECEIVED_VALUE, message);

                    final Intent notificationIntent = new Intent(App.Context(), LoginActivity.class);
                    notificationIntent.setAction(Intent.ACTION_MAIN);
                    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(notificationIntent);
                } else {
                    if (VM.MeetingsViewModel != null)
                        VM.MeetingsViewModel.OnMessageReceived(message);
                }
            }
        }
    }
}
