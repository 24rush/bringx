package com.rinf.bringx.ViewModels;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.IContextNotifier;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Model.Cargo;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.R;
import com.rinf.bringx.Views.LoginActivity;
import com.rinf.bringx.service.GPSTracker;
import com.rinf.bringx.storage.SettingsStorage;
import com.rinf.bringx.utils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class OrderedMeeting implements Comparable<OrderedMeeting> {

    @Override
    public int compareTo(OrderedMeeting another) {
        return ETA.compareTo(another.ETA);
    }

    public MeetingType Type;
    public String OrderId;
    public String OrderVersion;
    public Date ETA;

    public OrderedMeeting(MeetingType type, Meeting meeting) {
        Type = type;
        OrderId = meeting.OrderID;
        OrderVersion = meeting.OrderVersion;
        ETA = (type == MeetingType.Delivery ? meeting.ETADelivery : meeting.ETAPickup);
    }
}

public class MeetingsViewModel {
    private List<Meeting> MeetingsList = new ArrayList<Meeting>();
    private List<OrderViewModel> OrdersList = new ArrayList<OrderViewModel>();

    public OrderViewModel CurrentMeeting = new OrderViewModel();
    public OrderViewModel NextMeeting = new OrderViewModel();
    public Observable<Boolean> OnNoMoreJobs = new Observable<Boolean>(false);

    private List<OrderedMeeting> _orderedMeetings = new LinkedList<OrderedMeeting>();
    public Observable<String> StatusButton = new Observable<String>("");

    public Observable<Boolean> IsRetrievingData = new Observable<Boolean>(false);
    public Observable<Boolean> CanDisplayMeetings = new Observable<Boolean>(false);
    public Observable<Boolean> OnFirstMeetingChanged = new Observable<Boolean>(false);

    public Observable<Boolean> IsError = new Observable<Boolean>(false);
    public String Error;

    private String _currentAddress = "";

    private IStatusHandler<List<Meeting>, String> _meetingsListStatusHandler = new IStatusHandler<List<Meeting>, String>() {
        @Override
        public void OnError(com.rinf.bringx.utils.Error err, String... p) {
            IsRetrievingData.set(false);
            CanDisplayMeetings.set(false);

            Error = err.Message;
            IsError.set(true);
        }

        @Override
        public void OnSuccess(List<Meeting> response, String... p) {
            MeetingsList = response;

            IStatusHandler<List<Order>, Object> statusHandlerOrders = new IStatusHandler<List<Order>, Object>() {
                @Override
                public void OnError(com.rinf.bringx.utils.Error err, Object... p) {
                    IsRetrievingData.set(false);
                    CanDisplayMeetings.set(false);

                    Error = err.Message;
                    IsError.set(true);
                }

                @Override
                public void OnSuccess(List<Order> response, Object... p) {
                    OnFirstMeetingChanged.set(false);
                    IsRetrievingData.set(false);

                    // Here we should have all the orders we need
                    // Start by breaking them in pickup/delivery then sort them
                    // _orderedMeetings contains the old meetings
                    String firstMeetingAdr = null;
                    if (OrdersList.size() > 0) {
                        firstMeetingAdr = OrdersList.get(0).Address.get();
                    }

                    _orderedMeetings.clear();
                    for (Meeting meeting : MeetingsList) {
                        if (meeting.ETADelivery != null)
                            _orderedMeetings.add(new OrderedMeeting(MeetingType.Delivery, meeting));

                        if (meeting.ETAPickup != null)
                            _orderedMeetings.add(new OrderedMeeting(MeetingType.Pickup, meeting));
                    }

                    if (_orderedMeetings.size() > 0) {
                        // OrderedMeetings contains the ordered list of pickup and deliveries
                        Collections.sort(_orderedMeetings);
                    }

                    OrdersList.clear();
                    for (OrderedMeeting orderedJob : _orderedMeetings) {
                        for (int i = 0; i < response.size(); i++) {
                            Order order = response.get(i);

                            // Validate finalized Meetings
                            if (orderedJob.Type == MeetingType.Delivery && order.DeliveryAddress().Status() == "delivery-done")
                                continue;

                            if (orderedJob.Type == MeetingType.Pickup && order.PickupAddress().Status() == "pickup-done")
                                continue;

                            if (order.Id().equals(orderedJob.OrderId) && order.Version().equals(orderedJob.OrderVersion)) {
                                OrdersList.add(new OrderViewModel(orderedJob, order));
                                break;
                            }
                        }
                    }

                    if (OrdersList.size() > 0) {
                        // If there were no more orders and a new one arrives then trigger alert
                        // or if there is an actual order change
                        OrderViewModel newFirstMeeting = OrdersList.get(0);
                        if ((OnNoMoreJobs.get() == true && newFirstMeeting != null) ||
                                (firstMeetingAdr != null && newFirstMeeting != null && !firstMeetingAdr.equals(newFirstMeeting.Address.get())))
                        {
                            // Play sound and display alert
                            Log.d("First meeting changed");
                            OnFirstMeetingChanged.set(true);
                        }

                        // Aggregate order cargo
                        OrderViewModel agrMeeting = OrdersList.get(0);
                        for (int i = 1; i < OrdersList.size(); i++) {
                            OrderViewModel currOrder = OrdersList.get(i);

                            if (agrMeeting.Address.get().equals(currOrder.Address.get())) {
                                agrMeeting.AddAgrCargo(currOrder.Cargo());
                            } else {
                                agrMeeting = currOrder;
                            }
                        }

                        OnNoMoreJobs.set(false);
                        loadCurrentAndNextMeetings(false);
                    } else {
                        // No Orders to process
                        OnNoMoreJobs.set(true);
                    }

                    Intent gpsServiceIntent = new Intent(App.Context(), GPSTracker.class);
                    gpsServiceIntent.putExtra("ordersCount", String.valueOf(OrdersList.size() / 2 + OrdersList.size() % 2));
                    App.Context().startService(gpsServiceIntent);
                }
            };

            ServiceProxy proxy = new ServiceProxy(statusHandlerOrders);
            proxy.GetOrdersList(VM.LoginViewModel.UserName.get(), response, VM.LoginViewModel.DriverId.get(), VM.LoginViewModel.AuthToken.get());
        }
    };

    public MeetingsViewModel() {
        VM.LoginViewModel.IsLoggedIn.addObserverContext(new IContextNotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value, Object context) {
                CanDisplayMeetings.set(false);

                if (value == false)
                    return;

                OnNoMoreJobs.set(false);
                IsRetrievingData.set(true);
                ((MeetingsViewModel) context).GetMeetingsList();
            }
        }, this);

        CurrentMeeting.OnStatusChanged.addObserver(new INotifier<MEETING_STATUS>() {
            @Override
            public void OnValueChanged(MEETING_STATUS value) {
                IStatusHandler<Boolean, Object> statusHandler = new IStatusHandler<Boolean, Object>() {
                    @Override
                    public void OnError(com.rinf.bringx.utils.Error err, Object... p) {
                        Log.e("Error " + err.Message + "(" + err.Code + ") occurred on status update.");

                        // Error occurred during status update
                        String orderId = (String) p[0];
                        String status = (String) p[1];

                        if (err.Code != 500) {
                            Log.d("Saving to local cache for later update");
                            App.StorageManager().Setting().appendToKey(SettingsStorage.PENDING_STATUSES, orderId + "," + status);
                        }
                    }

                    @Override
                    public void OnSuccess(Boolean response, Object... p) {

                    }
                };

                ServiceProxy proxy = new ServiceProxy(statusHandler);
                proxy.SetMeetingStatus(CurrentMeeting.ParentOrderId + "-" + CurrentMeeting.ParentOrderVersion, OrderViewModel.mapStatus(value),
                        CurrentMeeting.ReasonRejected.get(), VM.LoginViewModel.DriverId.get(), VM.LoginViewModel.AuthToken.get());

                Log.d("Setting status " + value + " to " + CurrentMeeting.ParentOrderId + " comments " + CurrentMeeting.ReasonRejected.get());
                App.StorageManager().Orders().setString(CurrentMeeting.ParentOrderId, CurrentMeeting.ModelData().toString());

                if (value == MEETING_STATUS.PICKUP_DRIVING || value == MEETING_STATUS.DELIVERY_DRIVING) {
                    // Check to see if driver is at the same address
                    String nextAddress = CurrentMeeting.Address.get();

                    Log.d("curr address: " + _currentAddress + " next: " + nextAddress);

                    if (nextAddress.equals(_currentAddress)) {
                        CurrentMeeting.AdvanceOrderStatus("");

                        return;
                    }
                }

                if (value == MEETING_STATUS.PICKUP_DONE || value == MEETING_STATUS.DELIVERY_DONE) {
                    loadCurrentAndNextMeetings(true);
                }

                if (value == MEETING_STATUS.PICKUP_REJECTED || value == MEETING_STATUS.PICKUP_FAILED ||
                        value == MEETING_STATUS.DELIVERY_REJECTED || value == MEETING_STATUS.DELIVERY_FAILED) {
                    for (OrderViewModel order : OrdersList) {
                        if (order.ParentOrderId.equals(CurrentMeeting.ParentOrderId)) {
                            order.SetStatus(value);
                            OrdersList.remove(order);
                            break;
                        }
                    }

                    loadCurrentAndNextMeetings(true);
                }

                Localization locals = new Localization(App.Context());
                if (value == MEETING_STATUS.PICKUP_DRIVING || value == MEETING_STATUS.DELIVERY_DRIVING)
                    StatusButton.set(locals.getText(R.string.lbl_btn_arrived));
                else if (value == MEETING_STATUS.PICKUP_ARRIVED) {
                    StatusButton.set(locals.getText(R.string.lbl_btn_loaded));
                } else if (value == MEETING_STATUS.DELIVERY_ARRIVED) {
                    StatusButton.set(locals.getText(R.string.lbl_btn_done));
                }
            }
        });
    }

    public void OnInternetConnectionChanged() {
        if (App.DeviceManager().IsNetworkAvailable() == true) {
            if (VM.LoginViewModel.IsLoggedIn.get() == false) {
                return;
            }

            Log.d("Device is connected to Internet. Checking pending status updates.");

            String pendingStatuses = App.StorageManager().Setting().getString(SettingsStorage.PENDING_STATUSES);
            if (!pendingStatuses.isEmpty()) {
                ServiceProxy sp = new ServiceProxy(new IStatusHandler<Object, String>() {
                    @Override
                    public void OnError(com.rinf.bringx.utils.Error err, String... ctx) {

                    }

                    @Override
                    public void OnSuccess(Object response, String... ctx) {
                        Log.d("Removing key " + ctx[0] + " with status " + ctx[1]);
                        App.StorageManager().Setting().removeFromKey(SettingsStorage.PENDING_STATUSES, ctx[0] + "," + ctx[1]);
                    }
                });

                // Format <order-id>,status|...
                String[] pairs = pendingStatuses.split("\\|");
                for (String pair : pairs) {
                    String[] kv = pair.split(",");

                    sp.SetMeetingStatus(kv[0], kv[1], "", VM.LoginViewModel.DriverId.get(), VM.LoginViewModel.AuthToken.get());
                }
            }
        } else
            Log.d("Device is NOT connected to Internet");
    }

    private void loadCurrentAndNextMeetings(boolean removeHeadOfList) {
        if (removeHeadOfList && OrdersList.size() > 0) {
            OrdersList.remove(0);

            Intent gpsServiceIntent = new Intent(App.Context(), GPSTracker.class);
            gpsServiceIntent.putExtra("ordersCount", String.valueOf(OrdersList.size() / 2 + OrdersList.size() % 2));
            App.Context().startService(gpsServiceIntent);
        }

        if (OrdersList.size() > 0) {
            _currentAddress = CurrentMeeting.Address.get();
            CurrentMeeting.PreLoad(OrdersList.get(0));

            if (OrdersList.size() > 1) {
                int agrOrderCount = CurrentMeeting.AgrCargo.size();
                if (agrOrderCount > 0 && OrdersList.size() > agrOrderCount)
                    NextMeeting.PreLoad(OrdersList.get(agrOrderCount));
                else
                    NextMeeting.PreLoad(OrdersList.get(1));
            }

            CanDisplayMeetings.set(true);

            // Order is displayed
            CurrentMeeting.OnStatusChanged.set(CurrentMeeting.Type() == MeetingType.Pickup ? MEETING_STATUS.PICKUP_DRIVING : MEETING_STATUS.DELIVERY_DRIVING);
        } else {
            // No Orders to process
            OnNoMoreJobs.set(true);
            CanDisplayMeetings.set(false);
        }
    }

    public void OnPushReceived(List<Meeting> newMeetingsList) {
        if (newMeetingsList != null) {
            _meetingsListStatusHandler.OnSuccess(newMeetingsList);
        }
    }

    public void GetMeetingsList() {
        ServiceProxy proxy = new ServiceProxy(_meetingsListStatusHandler);
        proxy.GetMeetingsList(VM.LoginViewModel.UserName.get(), VM.LoginViewModel.DriverId.get(), VM.LoginViewModel.AuthToken.get());
    }
}