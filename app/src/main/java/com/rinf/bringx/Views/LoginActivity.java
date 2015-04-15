package com.rinf.bringx.Views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.rinf.bringx.EasyBindings.Bindings;
import com.rinf.bringx.EasyBindings.Controls;

import com.rinf.bringx.EasyBindings.ICommand;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Bindings.Mode;
import com.rinf.bringx.R;
import com.rinf.bringx.ViewModels.VM;
import com.rinf.bringx.service.GPSTracker;
import com.rinf.bringx.utils.DataEndpoint;
import com.rinf.bringx.utils.Localization;
import com.rinf.bringx.utils.Log;

import java.util.concurrent.Callable;

public class LoginActivity extends ActionBarActivity {

    private Bindings Bindings = new Bindings();
    private Controls Controls = new Controls(this);

    private Localization localization;
    private DataEndpoint dataEndpoint;

    private ProgressDialog loginProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        localization = new Localization(this);
        new VM();

        Bindings.BindVisible(Controls.get(R.id.layout_login), VM.LoginViewModel.IsLoggedIn, Mode.Invert);
        Bindings.BindVisible(Controls.get(R.id.layout_meetings), VM.LoginViewModel.IsLoggedIn);
        Bindings.BindChanged(VM.LoginViewModel.IsLoggedIn, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                invalidateOptionsMenu();

                if (value == false)
                    return;

                onSuccessfulLogin();
            }
        });

        Bindings.BindText(Controls.get(R.id.loginUserName), VM.LoginViewModel.UserName, Mode.TwoWay);
        Bindings.BindText(Controls.get(R.id.loginPassword), VM.LoginViewModel.Password, Mode.TwoWay);

        Bindings.BindChanged(VM.LoginViewModel.IsLoggingIn, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                if (value == true) {
                    loginProgressDialog = new ProgressDialog(LoginActivity.this);
                    loginProgressDialog.setMessage(localization.getText(R.string.lbl_login_in_progress));
                    loginProgressDialog.setCancelable(false);
                    loginProgressDialog.show();
                } else {
                    if (loginProgressDialog != null)
                        loginProgressDialog.dismiss();
                }
            }
        });

        Bindings.BindChanged(VM.LoginViewModel.IsError, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                if (value == true) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this)
                            .setTitle(localization.getText(R.string.msg_alert_login_error))
                            .setMessage(VM.LoginViewModel.Error)
                            .setPositiveButton(localization.getText(R.string.btn_ok), null);
                    alertDialog.show();
                }
            }
        });

        Bindings.BindCommand(Controls.get(R.id.btnLogin), new ICommand<Object>() {
            @Override
            public void Execute(Object context) {
                VM.LoginViewModel.DoLogin();
            }
        }, this);
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
                            public void onClick(DialogInterface dialog, int which) {
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

    private void onSuccessfulLogin() {
        // Check if alert should be triggered to enable GPS/3G
        GPSTracker tracker = new GPSTracker(this);
        tracker.CheckLocationProviders();

        if (!tracker.IsGPSEnabled() && !tracker.IsNetworkEnabled()) {
            showSettingsAlert();
        }

        dataEndpoint = new DataEndpoint();
        dataEndpoint.GetOrders();
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        boolean visible = VM.LoginViewModel.IsLoggedIn.get();

        menu.findItem(R.id.action_rejected_customer).setVisible(visible);
        menu.findItem(R.id.action_not_possible_driver).setVisible(visible);
        menu.findItem(R.id.action_logout).setVisible(visible);

        return true;
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
                showOKCancelDialog(R.string.msg_exit_app, new Callable() {
                    @Override
                    public Object call() throws Exception {
                        stopService(new Intent(LoginActivity.this, GPSTracker.class));
                        finish();

                        return null;
                    }
                });

                break;

            case R.id.action_call_operator:
                String uri = "tel:" + localization.getText(R.string.operator_phone_number);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);

                break;

            case R.id.action_logout:
                showOKCancelDialog(R.string.msg_logout_app, new Callable() {
                    @Override
                    public Object call() throws Exception {
                        VM.LoginViewModel.Logout();

                        return null;
                    }
                });

                break;

            case R.id.action_rejected_customer:
                showOKCancelDialog(R.string.msg_rejected_customer, new Callable() {
                    @Override
                    public Object call() throws Exception {
                        //TODO

                        return null;
                    }
                });

                break;

            case R.id.action_not_possible_driver:
                showOKCancelDialog(R.string.msg_rejected_customer, new Callable() {
                    @Override
                    public Object call() throws Exception {
                        //TODO

                        return null;
                    }
                });

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showOKCancelDialog(int msgId, final Callable onOk) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle(localization.getText(R.string.msg_alert_confirmation_title))
                .setMessage(localization.getText(msgId))

                .setPositiveButton(localization.getText(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    onOk.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .setNegativeButton(localization.getText(R.string.btn_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        alertDialog.show();
    }
}
