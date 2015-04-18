package com.rinf.bringx.ViewModels;

import com.rinf.bringx.EasyBindings.IContextNotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.utils.IStatusHandler;
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
    public Date ETA;

    public OrderedMeeting(MeetingType type, String id, Date eta) {
        Type = type;
        OrderId = id;
        ETA = eta;
    }
}

public class MeetingsViewModel {
    private List<Meeting> MeetingsList = new ArrayList<Meeting>();
    private List<OrderViewModel> OrdersList = new ArrayList<OrderViewModel>();

    public OrderViewModel CurrentMeeting = new OrderViewModel();

    private List<OrderedMeeting> _orderedMeetings = new LinkedList<OrderedMeeting>();

    public MeetingsViewModel() {
        VM.LoginViewModel.IsLoggedIn.addObserverContext(new IContextNotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value, Object context) {
                if (value == false)
                    return;

                ((MeetingsViewModel) context).GetMeetingsList();
            }
        }, this);
    }

    public void GetMeetingsList() {
        final IStatusHandler<List<Order>> statusHandlerOrders = new IStatusHandler<List<Order>>() {
            @Override
            public void OnError(com.rinf.bringx.utils.Error err) {

            }

            @Override
            public void OnSuccess(List<Order> response) {
                // Here we should have all the orders we need
                // Start by breaking them in pickup/delivery then sort them
                for (Meeting meeting : MeetingsList) {
                    if (meeting.ETADelivery != null)
                        _orderedMeetings.add(new OrderedMeeting(MeetingType.Pickup, meeting.OrderID, meeting.ETADelivery));

                    if (meeting.ETAPickup != null)
                        _orderedMeetings.add(new OrderedMeeting(MeetingType.Delivery, meeting.OrderID, meeting.ETAPickup));
                }

                // OrderedMeetings contains the ordered list of pickup and deliveries
                Collections.sort(_orderedMeetings);

                OrdersList.clear();
                for (OrderedMeeting orderedJob : _orderedMeetings)
                    for (int i = 0; i < response.size(); i++) {
                        if (response.get(i).Id().equals(orderedJob.OrderId)) {
                            OrdersList.add(new OrderViewModel(orderedJob.Type, orderedJob.ETA, response.get(i)));

                            if (OrdersList.size() == 1) {
                                CurrentMeeting.Load(orderedJob.Type, orderedJob.ETA, response.get(i));
                            }

                            break;
                        }
                    }
            }
        };

        IStatusHandler<List<Meeting>> statusHandler = new IStatusHandler<List<Meeting>>() {
            @Override
            public void OnError(com.rinf.bringx.utils.Error err) {

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
