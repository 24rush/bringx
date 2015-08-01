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

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(App.Context());
        String messageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            String newMeetingList = extras.getString("ml");

            if (newMeetingList != null) {
                Log.d("Received new meeting list: " + newMeetingList);

                MeetingsListTask meetingsListTask = new MeetingsListTask(null);
                List<Meeting> parsedMeetings = meetingsListTask.OnMeetingsListReceived(newMeetingList);

                if (parsedMeetings != null) {
                    if (App.IsVisible())
                        VM.MeetingsViewModel.OnPushReceived(parsedMeetings);
                    else {
                        App.StorageManager().Setting().setBoolean(SettingsStorage.FIST_MEETING_CHANGED, true);

                        final Intent notificationIntent = new Intent(App.Context(), LoginActivity.class);
                        notificationIntent.setAction(Intent.ACTION_MAIN);
                        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(notificationIntent);
                    }
                }
            }
        }
    }
}
