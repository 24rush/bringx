package com.rinf.bringx.ViewModels;

import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Model.Address;
import com.rinf.bringx.Model.Meeting;
import com.rinf.bringx.Model.Order;

import java.util.Date;

public class OrderViewModel {
    private Order _order;
    private MeetingType _type;

    public Observable<String> ETA = new Observable<String>();
    public Observable<String> Name = new Observable<String>();
    public Observable<String> Pieces = new Observable<String>();

    public Observable<String> Instructions = new Observable<String>();
    public Observable<String> Notes = new Observable<String>();

    public OrderViewModel() {

    }

    public OrderViewModel(OrderViewModel other) {
        ETA.set(other.ETA.get());
    }

    public OrderViewModel(MeetingType type, Date eta, Order modelData) {
        Load(type, eta, modelData);
    }

    public void Load(MeetingType type, Date eta, Order modelData) {
        _order = modelData;
        _type = type;

        Address address = _type == MeetingType.Delivery ? modelData.DeliveryAddress() : modelData.PickupAddress();

        ETA.set(eta.toString());
        Name.set(address.Name() + (!address.Company().isEmpty() ? ", " + address.Company() : ""));
        Pieces.set(_order.NumberGoods() + " Speisen");

        Instructions.set(address.Instructions() + "\na\nb\nc\nd\nf");
        Notes.set(address.Notes());
    }
}
