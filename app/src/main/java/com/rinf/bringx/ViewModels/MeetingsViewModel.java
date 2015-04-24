package com.rinf.bringx.ViewModels;

import android.app.ProgressDialog;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.IContextNotifier;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.R;
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
                        Log.e("Error " + err.Message + "(" + err.Code + ") occurred on status update. Saving to local cache.");

                        // Error occurred during status update
                        String orderId = (String) p[1];
                        String status = (String) p[2];

                        App.StorageManager().Setting().appendToKey(SettingsStorage.PENDING_STATUSES, orderId + "," + status);
                    }

                    @Override
                    public void OnSuccess(Boolean response, Object... p) {

                    }
                };

                ServiceProxy proxy = new ServiceProxy(statusHandler);
                proxy.SetMeetingStatus(VM.LoginViewModel.UserName.get(), CurrentMeeting.ParentOrderId, OrderViewModel.mapStatus(value));

                Log.d("Setting status " + value + " to " + CurrentMeeting.ParentOrderId);
                App.StorageManager().Orders().setString(CurrentMeeting.ParentOrderId, CurrentMeeting.ModelData().toString());

                if (value == MEETING_STATUS.LOADED || value == MEETING_STATUS.DELIVERY_DONE) {
                    loadCurrentAndNextMeetings(true);
                }

                if (value == MEETING_STATUS.REJECTED_CUSTOMER || value == MEETING_STATUS.REJECTED_DRIVER) {
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
                if (value == MEETING_STATUS.PICK_DRIVING || value == MEETING_STATUS.DELIVERY_DRIVING)
                    StatusButton.set(locals.getText(R.string.lbl_btn_arrived));
                else if (value == MEETING_STATUS.PICK_MEETING) {
                    StatusButton.set(locals.getText(R.string.lbl_btn_loaded));
                } else if (value == MEETING_STATUS.DELIVERY_MEETING) {
                    StatusButton.set(locals.getText(R.string.lbl_btn_done));
                }
            }
        });
    }

    public void OnInternetConnectionChanged() {
        if (App.DeviceManager().IsNetworkAvailable() == true) {
            Log.d("Device is connected to Internet. Checking pending status updates.");

            String pendingStatuses = App.StorageManager().Setting().getString(SettingsStorage.PENDING_STATUSES);
            if (!pendingStatuses.isEmpty()) {
                ServiceProxy sp = new ServiceProxy(new IStatusHandler<Object, String>() {
                    @Override
                    public void OnError(com.rinf.bringx.utils.Error err, String... ctx) {

                    }

                    @Override
                    public void OnSuccess(Object response, String... ctx) {
                        App.StorageManager().Setting().removeFromKey(SettingsStorage.PENDING_STATUSES, ctx[1] + "," + ctx[2]);
                    }
                });

                // Format <order-id>,status|...
                String[] pairs = pendingStatuses.split("\\|");
                for (String pair : pairs) {
                    String[] kv = pair.split(",");

                    sp.SetMeetingStatus(VM.LoginViewModel.UserName.get(), kv[0], kv[1]);
                }
            }
        }
        else
            Log.d("Device is NOT connected to Internet");
    }

    private void loadCurrentAndNextMeetings(boolean removeHeadOfList) {
        if (removeHeadOfList) {
            OrdersList.remove(0);
        }

        if (OrdersList.size() > 0) {
            CurrentMeeting.Load(OrdersList.get(0));

            if (OrdersList.size() > 1) {
                NextMeeting.PreLoad(OrdersList.get(1));
            }

            CanDisplayMeetings.set(true);
        } else {
            // No Orders to process
            OnNoMoreJobs.set(true);
            CanDisplayMeetings.set(false);
        }
    }

    public void GetMeetingsList() {
        IStatusHandler<List<Meeting>, String> statusHandler = new IStatusHandler<List<Meeting>, String>() {
            @Override
            public void OnError(com.rinf.bringx.utils.Error err, String... p) {
                IsRetrievingData.set(false);
                CanDisplayMeetings.set(false);
            }

            @Override
            public void OnSuccess(List<Meeting> response, String... p) {
                MeetingsList = response;

                IStatusHandler<List<Order>, Object> statusHandlerOrders = new IStatusHandler<List<Order>, Object>() {
                    @Override
                    public void OnError(com.rinf.bringx.utils.Error err, Object... p) {
                        IsRetrievingData.set(false);
                        CanDisplayMeetings.set(false);
                    }

                    @Override
                    public void OnSuccess(List<Order> response, Object... p) {
                        IsRetrievingData.set(false);

                        // Here we should have all the orders we need
                        // Start by breaking them in pickup/delivery then sort them
                        _orderedMeetings.clear();
                        for (Meeting meeting : MeetingsList) {
                            if (meeting.ETADelivery != null)
                                _orderedMeetings.add(new OrderedMeeting(MeetingType.Delivery, meeting));

                            if (meeting.ETAPickup != null)
                                _orderedMeetings.add(new OrderedMeeting(MeetingType.Pickup, meeting));
                        }

                        // OrderedMeetings contains the ordered list of pickup and deliveries
                        Collections.sort(_orderedMeetings);

                        OrdersList.clear();
                        for (OrderedMeeting orderedJob : _orderedMeetings) {
                            for (int i = 0; i < response.size(); i++) {
                                Order order = response.get(i);

                                // Validate finalized Meetings
                                if (orderedJob.Type == MeetingType.Delivery && order.DeliveryAddress().Status() == "delivery-success")
                                    continue;

                                if (orderedJob.Type == MeetingType.Pickup && order.PickupAddress().Status() == "loaded")
                                    continue;

                                if (order.Id().equals(orderedJob.OrderId) && order.Version().equals(orderedJob.OrderVersion)) {
                                    OrdersList.add(new OrderViewModel(orderedJob, order));
                                    break;
                                }
                            }
                        }

                        if (OrdersList.size() > 0) {
                            loadCurrentAndNextMeetings(false);
                        } else {
                            // No Orders to process
                            OnNoMoreJobs.set(true);
                        }
                    }
                };

                ServiceProxy proxy = new ServiceProxy(statusHandlerOrders);
                proxy.GetOrdersList(VM.LoginViewModel.UserName.get(), response);
            }
        };

        ServiceProxy proxy = new ServiceProxy(statusHandler);
        proxy.GetMeetingsList(VM.LoginViewModel.UserName.get());
    }
}
