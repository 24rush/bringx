package com.rinf.bringx.ViewModels;

import android.app.ProgressDialog;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.IContextNotifier;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.R;
import com.rinf.bringx.utils.IStatusHandler;
import com.rinf.bringx.utils.Localization;
import com.rinf.bringx.utils.ServiceProxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
                ServiceProxy proxy = new ServiceProxy(null);
                proxy.SetMeetingStatus(VM.LoginViewModel.UserName.get(), CurrentMeeting.ParentOrderId, value);

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
        final IStatusHandler<List<Order>> statusHandlerOrders = new IStatusHandler<List<Order>>() {
            @Override
            public void OnError(com.rinf.bringx.utils.Error err) {
                IsRetrievingData.set(false);
                CanDisplayMeetings.set(false);
            }

            @Override
            public void OnSuccess(List<Order> response) {
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

        IStatusHandler<List<Meeting>> statusHandler = new IStatusHandler<List<Meeting>>() {
            @Override
            public void OnError(com.rinf.bringx.utils.Error err) {
                IsRetrievingData.set(false);
                CanDisplayMeetings.set(false);
            }

            @Override
            public void OnSuccess(List<Meeting> response) {
                MeetingsList = response;

                ServiceProxy proxy = new ServiceProxy(statusHandlerOrders);
                proxy.GetOrdersList(VM.LoginViewModel.UserName.get(), response);
            }
        };

        ServiceProxy proxy = new ServiceProxy(statusHandler);
        proxy.GetMeetingsList(VM.LoginViewModel.UserName.get());
    }
}
