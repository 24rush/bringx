package com.rinf.bringx.ViewModels;

import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Model.Address;
import com.rinf.bringx.Model.Cargo;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.utils.StringAppender;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public class OrderViewModel {
    private Order _order;
    private MeetingType _type;

    public String ParentOrderId = "";
    public String ParentOrderVersion = "";

    public Observable<String> ETAHours = new Observable<String>("");
    public Observable<String> ETADate = new Observable<String>("");
    public Observable<String> Name = new Observable<String>("");
    public Observable<String> Address = new Observable<String>("");
    public Observable<String> Details = new Observable<String>("");

    public Observable<String> Instructions = new Observable<String>("");

    public Observable<String> FromTo = new Observable<String>("");
    public Observable<String> Pay = new Observable<String>("");

    public Observable<String> ReasonRejected = new Observable<String>("");

    public Observable<Boolean> IsDrivingMode = new Observable<Boolean>(true);
    public Observable<Boolean> IsMeetingMode = new Observable<Boolean>(false);

    public Observable<MEETING_STATUS> OnStatusChanged = new Observable<MEETING_STATUS>(MEETING_STATUS.PENDING);

    private Address _address;
    private Address _altAddress;

    private DecimalFormat _df = new DecimalFormat("#.00");

    public Address CurrentDestination() {
        return _address;
    }

    public Order ModelData() {
        return _order;
    }

    private List<Cargo> _cargo;

    public OrderViewModel() {

    }

    public OrderViewModel(OrderedMeeting job, Order modelData) {
        buildObject(job.Type, job.ETA, job.OrderId, job.OrderVersion, modelData);
    }

    public void Reset() {
        ParentOrderId = "";
        ParentOrderVersion = "";
        ETAHours.set("");
        ETADate.set("");
        Address.set("");
        Name.set("");
        Details.set("");
        Instructions.set("");
        FromTo.set("");
        Pay.set("");
    }

    public void PreLoad(OrderViewModel other) {
        _order = other._order;
        _type = other._type;
        _address = other._address;
        _cargo = other._cargo;

        ParentOrderId = other.ParentOrderId;
        ParentOrderVersion = other.ParentOrderVersion;

        ETAHours.set(other.ETAHours.get());
        ETADate.set(other.ETADate.get());
        Address.set(other.Address.get());
        Name.set(other.Name.get());
        Details.set(other.Details.get());
        Instructions.set(other.Instructions.get());
        FromTo.set(other.FromTo.get());
        Pay.set(other.Pay.get());
    }

    public void Load(OrderViewModel other) {
        PreLoad(other);

        // Order is displayed
        OnStatusChanged.set(_type == MeetingType.Pickup ? MEETING_STATUS.PICKUP_DRIVING : MEETING_STATUS.DELIVERY_DRIVING);
        IsMeetingMode.set(false);
        IsDrivingMode.set(true);
    }

    private void buildObject(MeetingType type, Date eta, String orderId, String orderVersion, Order modelData) {
        _order = modelData;
        _type = type;

        _address = _type == MeetingType.Delivery ? modelData.DeliveryAddress() : modelData.PickupAddress();

        ParentOrderId = orderId;
        ParentOrderVersion = orderVersion;

        ETAHours.set(android.text.format.DateFormat.format("hh:mm", eta) + "");
        ETADate.set(android.text.format.DateFormat.format(" - dd.MM.yyyy", eta) + " - " + ParentOrderId);

        String strAddress = "";
        strAddress = StringAppender.AppendIfFilled(strAddress, _address.Company(), _address.Street(), _address.Zip());
        Address.set(strAddress);

        Name.set(_address.Name());

        _altAddress = _type == MeetingType.Pickup ? modelData.DeliveryAddress() : modelData.PickupAddress();
        FromTo.set(_altAddress.Name());

        String cargo = "";
        Double cargoItemsPrice = 0.;

        _cargo = modelData.Cargo();
        if (_cargo != null) {
            for (Cargo item : _cargo) {
                cargo += item.Count() + "x " + item.Title() + "\n";
                cargoItemsPrice += (item.Count() * item.Price());
            }

            if (!cargo.isEmpty())
                cargo = cargo.substring(0, cargo.length() -1);
        }

        Details.set(_order.NumberGoods() + " goods\n" + cargo);

        String pay = "";
        Double priceGoods = (modelData.PriceGoods() != Double.NaN ? modelData.PriceGoods() : cargoItemsPrice);

        pay += "Total: " + _df.format(priceGoods + modelData.PriceDelivery()) + "\nGoods: " + _df.format(priceGoods) + "\nDelivery: " + _df.format(modelData.PriceDelivery());
        Pay.set(pay);

        String info = _address.Instructions() + (!_address.Notes().isEmpty() ? "\n" +  _address.Notes() : "");
        Instructions.set(!info.isEmpty() ? info : "--");
    }

    public void AdvanceOrderStatus() {
        MEETING_STATUS currentStatus = OnStatusChanged.get();

        switch (currentStatus) {
            case PICKUP_DRIVING:
                currentStatus = MEETING_STATUS.PICKUP_ARRIVED;
                IsMeetingMode.set(true);
                IsDrivingMode.set(false);
                break;

            case PICKUP_ARRIVED:
                currentStatus = MEETING_STATUS.PICKUP_DONE;
                break;

            case DELIVERY_DRIVING:
                currentStatus = MEETING_STATUS.DELIVERY_ARRIVED;
                IsMeetingMode.set(true);
                IsDrivingMode.set(false);
                break;

            case DELIVERY_ARRIVED:
                currentStatus = MEETING_STATUS.DELIVERY_DONE;
                break;
        }

        SetStatus(currentStatus);
    }

    public void SetStatus(MEETING_STATUS status) {
        String strStatus = mapStatus(status);

        if (strStatus == null)
            throw new InvalidParameterException("No mapping for status");

        _address.Status(strStatus);
        OnStatusChanged.set(status);
    }

    public static String mapStatus(MEETING_STATUS status) {
        switch (status){
            case DELIVERY_DONE:
                return "delivery-done";
            case DELIVERY_DRIVING:
                return "delivery-driving";
            case DELIVERY_ARRIVED:
                return "delivery-arrived";
            case PICKUP_DONE:
                return "pickup-done";
            case PENDING:
                return "pending";
            case PICKUP_DRIVING:
                return "pickup-driving";
            case PICKUP_ARRIVED:
                return "pickup-arrived";
            case PICKUP_REJECTED:
                return "pickup-rejected";
            case PICKUP_FAILED:
                return "pickup-failed";
            case DELIVERY_REJECTED:
                return "delivery-rejected";
            case DELIVERY_FAILED:
                return "delivery-failed";
            default:
                return null;
        }
    }

    public void Fail(String value) {
        ReasonRejected.set(value);

        if (_type == MeetingType.Delivery)
            SetStatus(MEETING_STATUS.DELIVERY_REJECTED);
        else
            SetStatus(MEETING_STATUS.PICKUP_REJECTED);
    }

    public void Reject(String value) {
        ReasonRejected.set(value);

        if (_type == MeetingType.Delivery)
            SetStatus(MEETING_STATUS.DELIVERY_FAILED);
        else
            SetStatus(MEETING_STATUS.PICKUP_FAILED);
    }
}
