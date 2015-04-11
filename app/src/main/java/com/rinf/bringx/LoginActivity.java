package com.rinf.bringx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.rinf.bringx.service.GPSTracker;
import com.rinf.bringx.utils.Localization;

public class LoginActivity extends ActionBarActivity {

    private Localization localization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        localization = new Localization(this);

        // Check if alert should be triggered to enable GPS/3G
        GPSTracker tracker = new GPSTracker(this);
        tracker.CheckLocationProviders();

        if (!tracker.IsGPSEnabled() && !tracker.IsNetworkEnabled()) {
            showSettingsAlert();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start the GPS service even if the providers are OFF and also when app is brought to foreground
        startService(new Intent(this, GPSTracker.class));
    }

    public void showSettingsAlert() {
        // Dialog to show when GPS is disabled
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
            .setTitle(localization.getText(R.string.msg_alert_title))
            .setMessage(localization.getText(R.string.msg_alert_ask_permission))

            .setPositiveButton(localization.getText(R.string.msg_alert_label_settings),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                    }
            })
            .setNegativeButton(localization.getText(R.string.msg_alert_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                    }
            });

        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit_app:
                stopService(new Intent(this, GPSTracker.class));
                finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
