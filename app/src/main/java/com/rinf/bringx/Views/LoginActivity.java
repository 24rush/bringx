package com.rinf.bringx.Views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.Bindings;
import com.rinf.bringx.EasyBindings.Bindings.Mode;
import com.rinf.bringx.EasyBindings.Controls;
import com.rinf.bringx.EasyBindings.ICommand;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.R;
import com.rinf.bringx.ViewModels.MEETING_STATUS;
import com.rinf.bringx.ViewModels.MeetingType;
import com.rinf.bringx.ViewModels.OrderViewModel;
import com.rinf.bringx.ViewModels.VM;
import com.rinf.bringx.service.GPSTracker;
import com.rinf.bringx.storage.SettingsStorage;
import com.rinf.bringx.utils.AlertGenerator;
import com.rinf.bringx.utils.ExpandableTextView;
import com.rinf.bringx.utils.Localization;
import com.rinf.bringx.utils.Log;
import com.rinf.bringx.utils.URLS;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


class ExpandableControl {
    private ExpandableTextView _control;
    private int _defaultLines;
    private int _expandedLines;

    private View _expanderOpen;
    private View _expanderClose;
    private View _expanderRound;

    private int _lastCurrentLines = 0;

    public ExpandableControl(View control, int defaultLines, int expandedLines) {
        _control = (ExpandableTextView) control;
        _defaultLines = defaultLines;
        _expandedLines = expandedLines;

        _control.setOnLayoutListener(new ExpandableTextView.OnLayoutListener() {
            @Override
            public void onLayout(TextView view) {
                setupLayout(view);
            }
        });

        _control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleExpand();
            }
        });
    }

    private void setupLayout(TextView view) {
        int currentLines = view.getLineCount();

        if (currentLines == _lastCurrentLines)
            return;

        _lastCurrentLines = currentLines;

        _expanderRound.setVisibility(View.VISIBLE);
        _expanderClose.setVisibility(View.GONE);
        _expanderOpen.setVisibility(View.GONE);

        Log.d("lines = " + currentLines + " " + _defaultLines);
        // If the control has more than one line that show arrow
        if (currentLines > _defaultLines) {
            _expanderRound.setVisibility(View.GONE);

            if (view.getMaxLines() > _defaultLines) {
                _expanderOpen.setVisibility(View.VISIBLE);
                _expanderClose.setVisibility(View.GONE);
            }
            else {
                _expanderOpen.setVisibility(View.GONE);
                _expanderClose.setVisibility(View.VISIBLE);
            }
        }
    }

    public ExpandableControl done() {
        setupLayout(_control);
        return this;
    }

    public ExpandableControl setExpanderOpen(View expanderOpen) {
        _expanderOpen = expanderOpen;
        return this;
    }

    public ExpandableControl setExpanderClose(View expanderClose) {
        _expanderClose = expanderClose;
        return this;
    }

    public ExpandableControl setExpanderRound(View expanderRound) {
        _expanderRound = expanderRound;
        return this;
    }

    public void ToggleExpand() {
        if (_control.getLineCount() == 1)
            return;

        Log.d("max line" + _control.getMaxLines() + "d " + _defaultLines + " " + _expandedLines);
        if (_control.getMaxLines() == _defaultLines) {
            _control.setMaxLines(_expandedLines);

            _expanderRound.setVisibility(View.GONE);
            _expanderOpen.setVisibility(View.VISIBLE);
            _expanderClose.setVisibility(View.GONE);
        } else {
            _control.setMaxLines(_defaultLines);

            _expanderRound.setVisibility(View.GONE);
            _expanderOpen.setVisibility(View.GONE);
            _expanderClose.setVisibility(View.VISIBLE);
        }
    }

    public void SetDefaultLines(int maxLines) {
        _control.setMaxLines(maxLines);
        _defaultLines = maxLines;
    }

    public void ResetExpand() {
        _expanderRound.setVisibility(View.VISIBLE);
        _expanderClose.setVisibility(View.GONE);
        _expanderOpen.setVisibility(View.GONE);

        _lastCurrentLines = 0;
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
    private MediaPlayer mMediaPlayer = null;

    private final String INTENT_PLAY_WARNING = "com.rinf.bringx.playWarning";

    private List<ExpandableControl> _expandableControls = new ArrayList<ExpandableControl>();

    private Observable<Boolean> IsConnectedInternet = new Observable<Boolean>(true);

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            VM.MeetingsViewModel.OnInternetConnectionChanged();

            IsConnectedInternet.set(App.DeviceManager().IsNetworkAvailable());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(URLS.IsDebug == false ? Color.parseColor("#53284f") : Color.RED));
        actionBar.setCustomView(R.layout.custom_action_bar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        localization = new Localization(this);

        if (App.DeviceManager().IsNetworkAvailable() == false) {
            AlertGenerator.ShowOkAlert(LoginActivity.this, R.string.msg_alert_information_title, R.string.msg_alert_no_internet_connection, null);
            IsConnectedInternet.set(false);
        }

        onInternetConnectionEstablished();
    }

    private void onInternetConnectionEstablished() {
        new VM();

        Bindings.BindVisible(Controls.get(R.id.imageInternet), IsConnectedInternet);
        Bindings.BindVisible(Controls.get(R.id.imageNoInternet), IsConnectedInternet, Mode.Invert);

        Bindings.BindVisible(Controls.get(R.id.layout_meetings), VM.MeetingsViewModel.CanDisplayMeetings);
        Bindings.BindChanged(VM.LoginViewModel.IsLoggedIn, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                Controls.get(R.id.layout_login).setVisibility(value == false ? View.VISIBLE : View.GONE);

                invalidateOptionsMenu();

                if (value == false) {
                    Controls.get(R.id.lbl_no_more_jobs).setVisibility(View.GONE);
                    Controls.get(R.id.layout_meetings).setVisibility(View.GONE);
                    Controls.get(R.id.layout_login).setVisibility(View.VISIBLE);
                    return;
                }

                onSuccessfulLogin();
            }
        });

        Bindings.BindText(Controls.get(R.id.loginUserName), VM.LoginViewModel.UserName, Mode.TwoWay);
        Bindings.BindText(Controls.get(R.id.loginPassword), VM.LoginViewModel.Password, Mode.TwoWay);

        Bindings.BindChanged(VM.LoginViewModel.IsLoggingIn, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(final Boolean value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
            }
        });

        Bindings.BindChanged(VM.LoginViewModel.IsError, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(final Boolean value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (value == true) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle(localization.getText(R.string.msg_alert_login_error))
                                    .setMessage(VM.LoginViewModel.Error)
                                    .setPositiveButton(localization.getText(R.string.btn_ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            VM.LoginViewModel.IsError.set(false);
                                        }
                                    });
                            alertDialog.show();
                        }
                    }
                });
            }
        });

        Bindings.BindCommand(Controls.get(R.id.btnLogin), new ICommand<Object>() {
            @Override
            public void Execute(Object context) {
                new AsyncTask<Void, Void, Integer>() {

                    @Override
                    protected Integer doInBackground(Void... params) {
                        App.DeviceManager().RegisterForPushNotifications(LoginActivity.this);

                        return 0;
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        VM.LoginViewModel.DoLogin();
                    }

                }.execute();
            }
        }, this);

        Bindings.BindChanged(VM.MeetingsViewModel.IsRetrievingData, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(final Boolean value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (value == true) {
                            loginProgressDialog = new ProgressDialog(LoginActivity.this);
                            loginProgressDialog.setMessage(getString(R.string.msg_alert_getting_jobs));
                            loginProgressDialog.setCancelable(false);
                            loginProgressDialog.show();
                        } else {
                            if (loginProgressDialog != null)
                                loginProgressDialog.dismiss();
                        }
                    }
                });
            }
        });

        createBindingsForMeetingsList();
    }

    private void createBindingsForMeetingsList() {
        Bindings.BindText(Controls.get(R.id.value_meeting_eta_hours), VM.MeetingsViewModel.CurrentMeeting.ETAHours);
        Bindings.BindText(Controls.get(R.id.value_meeting_eta_date), VM.MeetingsViewModel.CurrentMeeting.ETADate);
        Bindings.BindText(Controls.get(R.id.value_meeting_destination), VM.MeetingsViewModel.CurrentMeeting.Name);
        Bindings.BindText(Controls.get(R.id.value_meeting_address), VM.MeetingsViewModel.CurrentMeeting.Address);
        Bindings.BindText(Controls.get(R.id.value_meeting_details), VM.MeetingsViewModel.CurrentMeeting.Details);
        Bindings.BindText(Controls.get(R.id.value_meeting_info), VM.MeetingsViewModel.CurrentMeeting.Instructions);

        // Click on Name - call Phone number
        Bindings.BindCommand(Controls.get(R.id.value_meeting_destination), new ICommand<OrderViewModel>() {
            @Override
            public void Execute(OrderViewModel context) {
                if (context.CurrentDestination().Phone().isEmpty()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.no_phone_details), Toast.LENGTH_SHORT).show();
                    return;
                }

                String uri = "tel:" + context.CurrentDestination().Phone();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        }, VM.MeetingsViewModel.CurrentMeeting);

        ICommand<OrderViewModel> onClickAddressName = new ICommand<OrderViewModel>() {
            @Override
            public void Execute(OrderViewModel context) {
                Intent intent = null;
                String location = "";

                /*if (context.CurrentDestination().HasCoordinates()) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("geo:%s,%s", context.CurrentDestination().Latitude(),
                            context.CurrentDestination().Longitude())));
                } else {*/
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("geo:0,0?q=%s", URLEncoder.encode(context.Address.get()))));


                startActivity(intent);
            }
        };

        Bindings.BindCommand(Controls.get(R.id.value_meeting_address), onClickAddressName, VM.MeetingsViewModel.CurrentMeeting);

        _expandableControls.clear();
        _expandableControls.add((new ExpandableControl(Controls.get(R.id.value_meeting_details), 1, 50))
                .setExpanderRound(Controls.get(R.id.expander_round_meeting_details))
                .setExpanderClose(Controls.get(R.id.expander_close_meeting_details))
                .setExpanderOpen(Controls.get(R.id.expander_open_meeting_details)).done());

        _expandableControls.add((new ExpandableControl(Controls.get(R.id.value_meeting_info), 4, 50))
                .setExpanderRound(Controls.get(R.id.expander_round_meeting_info))
                .setExpanderClose(Controls.get(R.id.expander_close_meeting_info))
                .setExpanderOpen(Controls.get(R.id.expander_open_meeting_info)).done()
        );
        _expandableControls.add((new ExpandableControl(Controls.get(R.id.value_meeting_pay), 1, 4))
                .setExpanderRound(Controls.get(R.id.expander_round_meeting_pay))
                .setExpanderClose(Controls.get(R.id.expander_close_meeting_pay))
                .setExpanderOpen(Controls.get(R.id.expander_open_meeting_pay)).done());

        Bindings.BindText(Controls.get(R.id.btn_order_status), VM.MeetingsViewModel.StatusButton);
        Bindings.BindCommand(Controls.get(R.id.btn_order_status), new ICommand<Object>() {
            @Override
            public void Execute(Object context) {

                MEETING_STATUS value = VM.MeetingsViewModel.CurrentMeeting.OnStatusChanged.get();

                // If step is Arrived or Loaded check for confirmation
                if (value == MEETING_STATUS.PICKUP_ARRIVED || value == MEETING_STATUS.DELIVERY_ARRIVED) {
                    showOKCancelDialog(R.string.msgAnyComments, new INotifier<String>() {
                        @Override
                        public void OnValueChanged(String comments) {
                            VM.MeetingsViewModel.CurrentMeeting.AdvanceOrderStatus(comments);
                            resetExpandableControls();
                        }
                    }, R.layout.rejected_form_layout);
                }
                else {
                    VM.MeetingsViewModel.CurrentMeeting.AdvanceOrderStatus("");
                    resetExpandableControls();
                }
            }
        }, null);

        VM.MeetingsViewModel.OnNoMoreJobs.addObserver(new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                Controls.get(R.id.layout_meetings).setVisibility(value == true ? View.GONE : View.VISIBLE);
                Controls.get(R.id.lbl_no_more_jobs).setVisibility(value == true ? View.VISIBLE : View.GONE);
                invalidateOptionsMenu();
            }
        });

        VM.MeetingsViewModel.OnFirstMeetingChanged.addObserver(new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value) {
                if (value == false)
                    return;

                Log.d("OnFirstMeeting changed app is visible: " + App.IsVisible());

                if (!App.IsVisible()) {
                    App.StorageManager().Setting().setBoolean(SettingsStorage.FIST_MEETING_CHANGED, true);

                    final Intent notificationIntent = new Intent(App.Context(), LoginActivity.class);
                    notificationIntent.setAction(Intent.ACTION_MAIN);
                    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(notificationIntent);
                } else {
                    playSoundAndDisplayAlert();
                }
            }
        });

        Bindings.BindChanged(VM.MeetingsViewModel.IsError, new INotifier<Boolean>() {
            @Override
            public void OnValueChanged(final Boolean value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (value == true) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle(localization.getText(R.string.msg_alert_meetings_error))
                                    .setMessage(VM.MeetingsViewModel.Error)
                                    .setPositiveButton(localization.getText(R.string.btn_ok), null);
                            alertDialog.show();
                        }
                    }
                });
            }
        });

        // Next
        Bindings.BindVisible(Controls.get(R.id.layout_row_next), VM.MeetingsViewModel.CurrentMeeting.IsDrivingMode);
        Bindings.BindText(Controls.get(R.id.value_meeting_next), VM.MeetingsViewModel.NextMeeting.Address);
        Bindings.BindCommand(Controls.get(R.id.value_meeting_next), onClickAddressName, VM.MeetingsViewModel.NextMeeting);

        // From/To
        Bindings.BindVisible(Controls.get(R.id.layout_row_fromto), VM.MeetingsViewModel.CurrentMeeting.IsMeetingMode);
        Bindings.BindText(Controls.get(R.id.value_meeting_fromTo), VM.MeetingsViewModel.CurrentMeeting.FromTo);

        Bindings.BindCommand(Controls.get(R.id.layout_row_fromto), new ICommand<OrderViewModel>() {
            @Override
            public void Execute(OrderViewModel context) {
                if (context.AlternateDestination().Phone().isEmpty()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.no_phone_details), Toast.LENGTH_SHORT).show();
                    return;
                }

                String uri = "tel:" + context.AlternateDestination().Phone();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        }, VM.MeetingsViewModel.CurrentMeeting);

        // Pay
        Bindings.BindVisible(Controls.get(R.id.layout_row_pay), VM.MeetingsViewModel.CurrentMeeting.IsMeetingMode);
        Bindings.BindText(Controls.get(R.id.value_meeting_pay), VM.MeetingsViewModel.CurrentMeeting.Pay);
    }

    private void resetExpandableControls()
    {
        for (ExpandableControl exp : _expandableControls) {
            exp.ResetExpand();
        }

        _expandableControls.get(1).SetDefaultLines(4);
        // !!!WKA: Expand details in meeting mode screen
        if (VM.MeetingsViewModel.CurrentMeeting.IsMeetingMode.get() == true) {
            _expandableControls.get(1).SetDefaultLines(2);

            _expandableControls.get(0).ToggleExpand();
            _expandableControls.get(2).ToggleExpand();
        }

        TextView fromTo = (TextView) Controls.get(R.id.lbl_meeting_fromTo);
        if (VM.MeetingsViewModel.CurrentMeeting.Type() == MeetingType.Pickup)
            fromTo.setText(localization.getText(R.string.str_meeting_to));
        else
            fromTo.setText(localization.getText(R.string.str_meeting_from));
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.OnActivityResumed();
        Log.d("onResume");

        registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (App.wasInBackground)
        {
            //Do specific came-here-from-background code
        }

        App.stopActivityTransitionTimer();

        if (VM.LoginViewModel != null && VM.LoginViewModel.IsLoggedIn.get() == true &&
                App.StorageManager().Setting().getBoolean(SettingsStorage.FIST_MEETING_CHANGED) == true) {
            App.StorageManager().Setting().setBoolean(SettingsStorage.FIST_MEETING_CHANGED, false);

            playSoundAndDisplayAlert();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause");
        App.OnActivityPaused();
        App.startActivityTransitionTimer();

        unregisterReceiver(mConnReceiver);

        if (mMediaPlayer != null) {
            Log.d("onPause mediaPlayer");
            //mMediaPlayer.stop();
            //mMediaPlayer = null;
        }
    }

    private synchronized void playSoundAndDisplayAlert() {
        try {
            Log.d("playSound app visible: " + App.IsVisible());
            if (!App.IsVisible())
            {
                PowerManager pm = (PowerManager) App.Context().getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK,"BringxLock");
                wl.acquire(10000);
            }

            if (mMediaPlayer != null || !App.IsVisible()) {
                Log.d("Sound alert already playing or app is in background");
                return;
            }

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(App.Context(), soundUri);
            final AudioManager audioManager = (AudioManager) App.Context().getSystemService(Context.AUDIO_SERVICE);

            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }

            AlertGenerator.ShowOkAlert(LoginActivity.this, R.string.msg_alert_confirmation_title, R.string.msg_alert_first_meeting_changed, new Callable() {
                @Override
                public Object call() throws Exception {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.stop();
                        mMediaPlayer = null;
                    }

                    return null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        // Start the GPS service even if the providers are OFF and also when app is brought to foreground
        Intent gpsServiceIntent = new Intent(this, GPSTracker.class);
        gpsServiceIntent.putExtra("uid", VM.LoginViewModel.DriverId.get());
        gpsServiceIntent.putExtra("mobileid", App.DeviceManager().DeviceId());
        startService(gpsServiceIntent);

        if (App.StorageManager().Setting().getBoolean(SettingsStorage.FIST_MEETING_CHANGED) == true) {
            playSoundAndDisplayAlert();

            App.StorageManager().Setting().setBoolean(SettingsStorage.FIST_MEETING_CHANGED, false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean visible = VM.LoginViewModel != null && VM.LoginViewModel.IsLoggedIn.get();

        menu.findItem(R.id.action_rejected_customer).setVisible(visible && (VM.MeetingsViewModel.OnNoMoreJobs.get() == false));
        menu.findItem(R.id.action_not_possible_driver).setVisible(visible && (VM.MeetingsViewModel.OnNoMoreJobs.get() == false));

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
                showOKCancelDialog(R.string.msg_exit_app, new INotifier<String>() {
                    @Override
                    public void OnValueChanged(String value) {
                        VM.LoginViewModel.Logout(new Runnable() {
                            @Override
                            public void run() {
                                stopService(new Intent(LoginActivity.this, GPSTracker.class));
                                //finish();
                                //System.exit(0);
                            }
                        });
                    }
                }, 0);

                break;

            case R.id.action_call_operator:
                String uri = "tel:" + localization.getText(R.string.operator_phone_number);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);

                break;

            case R.id.action_rejected_customer:
                VM.MeetingsViewModel.CurrentMeeting.ReasonRejected.set("");
                showOKCancelDialog(R.string.msg_rejected_customer, new INotifier<String>() {
                    @Override
                    public void OnValueChanged(String value) {
                        VM.MeetingsViewModel.CurrentMeeting.Reject(value);
                    }
                }, R.layout.rejected_form_layout);

                break;

            case R.id.action_not_possible_driver:
                VM.MeetingsViewModel.CurrentMeeting.ReasonRejected.set("");
                showOKCancelDialog(R.string.msg_rejected_customer, new INotifier<String>() {
                    @Override
                    public void OnValueChanged(String value) {
                        VM.MeetingsViewModel.CurrentMeeting.Fail(value);
                    }
                }, R.layout.rejected_form_layout);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showOKCancelDialog(int msgId, final INotifier<String> onOk, int layoutId) {
        AlertGenerator.ShowOkCancelAlert(this, R.string.msg_alert_confirmation_title, msgId, onOk, null, layoutId);
    }
}
