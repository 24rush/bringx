package com.rinf.bringx.ViewModels;

import com.rinf.bringx.EasyBindings.IContextNotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.utils.IStatusHandler;
import com.rinf.bringx.utils.ServiceProxy;

import java.util.List;

public class MeetingsViewModel {
    public Observable<List<Meeting>> MeetingsList = new Observable<List<Meeting>>();
    public Observable<List<Order>> OrdersList = new Observable<List<Order>>();

    public MeetingsViewModel() {
        VM.LoginViewModel.IsLoggedIn.addObserverContext(new IContextNotifier<Boolean>() {
            @Override
            public void OnValueChanged(Boolean value, Object context) {
                if (value == false)
                    return;

                ((MeetingsViewModel)context).GetMeetingsList();
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
                OrdersList.set(response);
            }
        };

        IStatusHandler<List<Meeting>> statusHandler = new IStatusHandler<List<Meeting>>() {
            @Override
            public void OnError(com.rinf.bringx.utils.Error err) {

            }

            @Override
            public void OnSuccess(List<Meeting> response) {
                MeetingsList.set(response);

                ServiceProxy proxy = new ServiceProxy(statusHandlerOrders);
                proxy.GetOrdersList(VM.LoginViewModel.UserName.get(), response);
            }
        };

        ServiceProxy proxy = new ServiceProxy(statusHandler);
        proxy.GetMeetingsList(VM.LoginViewModel.UserName.get());
    }
}
