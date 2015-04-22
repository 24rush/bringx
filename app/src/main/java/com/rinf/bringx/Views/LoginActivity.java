package com.rinf.bringx.Views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.Bindings;
import com.rinf.bringx.EasyBindings.Controls;
import com.rinf.bringx.EasyBindings.ICommand;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Bindings.Mode;
import com.rinf.bringx.R;
import com.rinf.bringx.ViewModels.MEETING_STATUS;
import com.rinf.bringx.ViewModels.OrderViewModel;
import com.rinf.bringx.ViewModels.VM;
import com.rinf.bringx.service.GPSTracker;
import com.rinf.bringx.utils.AlertGenerator;
import com.rinf.bringx.utils.Localization;
import com.rinf.bringx.utils.Log;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

class ExpandableControl {
    private TextView _control;
    private int _defaultLines;
    private int _expandedLines;

    public ExpandableControl(View control, int defaultLines, int expandedLines) {
        _control = (TextView) control;
        _defaultLines = defaultLines;
        _expandedLines = expandedLines;
    }

    public void ToggleExpand() {
        if (_control.getMaxLines() == _defaultLines)
            _control.setMaxLines(_expandedLines);
        else
            _control.setMaxLines(_defaultLines);
    }

    public void ResetExpand() {
        _control.setMaxLines(_defaultLines);
    }

    public TextView Control() {
        return _control;
    }
}

public class LoginActivity extends ActionBarActivity {

    private Bindings Bindings = new Bindings();
    private Controls Controls = new Controls(this);

    private Localization localization;
    private ProgressDialog loginProgressDialog;

    private List<ExpandableControl> _expandableControls = new ArrayList<ExpandableControl>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.custom_action_bar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        localization = new Localization(this);
        new VM();

        Bindings.BindVisible(Controls.get(R.id.layout_login), VM.LoginViewModel.IsLoggedIn, Mode.Invert);
        Bindings.BindVisible(Controls.get(R.id.layout_meetings), VM.MeetingsViewModel.CanDisplayMeetings);
        Bindings.BindChanged(VM.LoginViewModel.IsLoggedIn, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                invalidateOptionsMenu();

                if (value == false) {
                    Controls.get(R.id.lbl_no_more_jobs).setVisibility(View.GONE);
                    return;
                }

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

        Bindings.BindChanged(VM.MeetingsViewModel.IsRetrievingData, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                if (value == true) {
                    loginProgressDialog = new ProgressDialog(LoginActivity.this);
                    loginProgressDialog.setMessage(getString(R.string.msg_alert_getting_jobs));
                    loginProgressDialog.setCancelable(false);
                    loginProgressDialog.show();
                }
                else {
                    if (loginProgressDialog != null)
                        loginProgressDialog.dismiss();
                }
            }
        });

        createBindingsForMeetingsList();
    }

    private void createBindingsForMeetingsList() {
        Bindings.BindText(Controls.get(R.id.value_meeting_eta), VM.MeetingsViewModel.CurrentMeeting.ETA);
        Bindings.BindText(Controls.get(R.id.value_meeting_destination), VM.MeetingsViewModel.CurrentMeeting.Name);
        Bindings.BindText(Controls.get(R.id.value_meeting_address), VM.MeetingsViewModel.CurrentMeeting.Address);
        Bindings.BindText(Controls.get(R.id.value_meeting_details), VM.MeetingsViewModel.CurrentMeeting.Details);
        Bindings.BindText(Controls.get(R.id.value_meeting_info), VM.MeetingsViewModel.CurrentMeeting.Instructions);
        Bindings.BindText(Controls.get(R.id.value_meeting_notes), VM.MeetingsViewModel.CurrentMeeting.Notes);

        // Click on Name - call Phone number
        Bindings.BindCommand(Controls.get(R.id.value_meeting_destination), new ICommand<OrderViewModel>() {
            @Override
            public void Execute(OrderViewModel context) {
                if (context.CurrentDestination().Phone().isEmpty()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.no_phone_details), Toast.LENGTH_SHORT).show();
                    return;
                }

                String uri = "tel:" + context.CurrentDestination().Phone();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        }, VM.MeetingsViewModel.CurrentMeeting);

        ICommand<OrderViewModel> onClickAddressName = new ICommand<OrderViewModel>() {
            @Override
            public void Execute(OrderViewModel context) {
                Intent intent = null;
                String location = "";

                if (context.CurrentDestination().HasCoordinates()) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("geo:%s,%s", context.CurrentDestination().Latitude(),
                            context.CurrentDestination().Longitude())));
                } else {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("geo:0,0?q=%s", URLEncoder.encode(context.Address.get()))));
                }

                startActivity(intent);
            }
        };

        Bindings.BindCommand(Controls.get(R.id.value_meeting_address), onClickAddressName, VM.MeetingsViewModel.CurrentMeeting);

        _expandableControls.add(new ExpandableControl(Controls.get(R.id.value_meeting_details), 1, 4));
        _expandableControls.add(new ExpandableControl(Controls.get(R.id.value_meeting_info), 4, 50));
        _expandableControls.add(new ExpandableControl(Controls.get(R.id.value_meeting_pay), 1, 4));

        for (ExpandableControl exp : _expandableControls) {
            Bindings.BindCommand(exp.Control(), new ICommand<ExpandableControl>() {
                @Override
                public void Execute(ExpandableControl context) {
                    context.ToggleExpand();
                }
            }, exp);
        }

        Bindings.BindText(Controls.get(R.id.btn_order_status), VM.MeetingsViewModel.StatusButton);
        Bindings.BindCommand(Controls.get(R.id.btn_order_status), new ICommand<Object>() {
            @Override
            public void Execute(Object context) {
                VM.MeetingsViewModel.CurrentMeeting.AdvanceOrderStatus();

                for (ExpandableControl exp : _expandableControls) {
                    exp.ResetExpand();
                }
            }
        }, null);

        VM.MeetingsViewModel.OnNoMoreJobs.addObserver(new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                if (value == false)
                    return;

                Controls.get(R.id.layout_meetings).setVisibility(View.GONE);
                Controls.get(R.id.lbl_no_more_jobs).setVisibility(View.VISIBLE);
                invalidateOptionsMenu();

                AlertGenerator.ShowOkAlert(LoginActivity.this, R.string.msg_alert_no_jobs_msg_title, R.string.msg_alert_no_jobs_msg, null);
            }
        });

        // Next
        Bindings.BindVisible(Controls.get(R.id.layout_row_next), VM.MeetingsViewModel.CurrentMeeting.IsDrivingMode);
        Bindings.BindText(Controls.get(R.id.value_meeting_next), VM.MeetingsViewModel.NextMeeting.Name);
        Bindings.BindCommand(Controls.get(R.id.value_meeting_next), onClickAddressName, VM.MeetingsViewModel.NextMeeting);

        // From/To
        Bindings.BindVisible(Controls.get(R.id.layout_row_fromto), VM.MeetingsViewModel.CurrentMeeting.IsMeetingMode);
        Bindings.BindText(Controls.get(R.id.value_meeting_fromTo), VM.MeetingsViewModel.CurrentMeeting.FromTo);

        // Pay
        Bindings.BindVisible(Controls.get(R.id.layout_row_pay), VM.MeetingsViewModel.CurrentMeeting.IsMeetingMode);
        Bindings.BindText(Controls.get(R.id.value_meeting_pay), VM.MeetingsViewModel.CurrentMeeting.Pay);
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
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean visible = VM.LoginViewModel.IsLoggedIn.get();

        menu.findItem(R.id.action_rejected_customer).setVisible(visible && (VM.MeetingsViewModel.OnNoMoreJobs.get() == false));
        menu.findItem(R.id.action_not_possible_driver).setVisible(visible && (VM.MeetingsViewModel.OnNoMoreJobs.get() == false));
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
                        VM.MeetingsViewModel.CurrentMeeting.SetStatus(MEETING_STATUS.REJECTED_CUSTOMER);
                        return null;
                    }
                });

                break;

            case R.id.action_not_possible_driver:
                showOKCancelDialog(R.string.msg_rejected_customer, new Callable() {
                    @Override
                    public Object call() throws Exception {
                        VM.MeetingsViewModel.CurrentMeeting.SetStatus(MEETING_STATUS.REJECTED_DRIVER);
                        return null;
                    }
                });

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showOKCancelDialog(int msgId, final Callable onOk) {
        AlertGenerator.ShowOkCancelAlert(this, R.string.msg_alert_confirmation_title, msgId, onOk, null);
    }
}
