package com.rinf.bringx.ViewModels;

import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Model.Address;
import com.rinf.bringx.Model.Cargo;
import com.rinf.bringx.Model.Order;
import com.rinf.bringx.utils.StringAppender;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.List;

public class OrderViewModel {
    private Order _order;
    private MeetingType _type;

    public String ParentOrderId = "";
    public Observable<String> ETA = new Observable<String>();
    public Observable<String> Name = new Observable<String>();
    public Observable<String> Address = new Observable<String>();
    public Observable<String> Details = new Observable<String>();

    public Observable<String> Instructions = new Observable<String>();

    public Observable<String> FromTo = new Observable<String>("");
    public Observable<String> Pay = new Observable<String>("");

    public Observable<String> ReasonRejected = new Observable<String>("");

    public Observable<Boolean> IsDrivingMode = new Observable<Boolean>(true);
    public Observable<Boolean> IsMeetingMode = new Observable<Boolean>(false);

    public Observable<MEETING_STATUS> OnStatusChanged = new Observable<MEETING_STATUS>(MEETING_STATUS.PENDING);

    private Address _address;
    private Address _altAddress;

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
        buildObject(job.Type, job.ETA, job.OrderId, modelData);
    }

    public void PreLoad(OrderViewModel other) {
        _order = other._order;
        _type = other._type;
        _address = other._address;
        _cargo = other._cargo;

        ParentOrderId = other.ParentOrderId;
        ETA.set(other.ETA.get());
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
        OnStatusChanged.set(_type == MeetingType.Pickup ? MEETING_STATUS.PICK_DRIVING : MEETING_STATUS.DELIVERY_DRIVING);
        IsMeetingMode.set(false);
        IsDrivingMode.set(true);
    }

    private void buildObject(MeetingType type, Date eta, String orderId, Order modelData) {
        _order = modelData;
        _type = type;

        _address = _type == MeetingType.Delivery ? modelData.DeliveryAddress() : modelData.PickupAddress();

        ParentOrderId = orderId;

        ETA.set(android.text.format.DateFormat.format("hh:mm - dd.MM.yyyy", eta) + " - " + ParentOrderId);

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
        }

        Details.set(_order.NumberGoods() + " goods\n" + cargo);

        String pay = "";
        Double priceGoods = (modelData.PriceGoods() != Double.NaN ? modelData.PriceGoods() : cargoItemsPrice);

        pay += "Total: " + (priceGoods + modelData.PriceDelivery()) + "\nGoods: " + priceGoods + "\nDelivery: " + modelData.PriceDelivery();
        Pay.set(pay);

        String info = _address.Instructions() + (!_address.Notes().isEmpty() ? "\n" +  _address.Notes() : "");
        Instructions.set(!info.equals("\n") ? info : "--");
    }

    public void AdvanceOrderStatus() {
        MEETING_STATUS currentStatus = OnStatusChanged.get();

        switch (currentStatus) {
            case PICK_DRIVING:
                currentStatus = MEETING_STATUS.PICK_MEETING;
                IsMeetingMode.set(true);
                IsDrivingMode.set(false);
                break;

            case PICK_MEETING:
                currentStatus = MEETING_STATUS.LOADED;
                break;

            case DELIVERY_DRIVING:
                currentStatus = MEETING_STATUS.DELIVERY_MEETING;
                IsMeetingMode.set(true);
                IsDrivingMode.set(false);
                break;

            case DELIVERY_MEETING:
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
                return "delivery-success";
            case DELIVERY_DRIVING:
                return "delivery-driving";
            case DELIVERY_MEETING:
                return "delivery-meeting";
            case LOADED:
                return "loaded";
            case PENDING:
                return "pending";
            case PICK_DRIVING:
                return "pick-driving";
            case PICK_MEETING:
                return "pick-meeting";
            case REJECTED_CUSTOMER:
                return "rejected";
            case REJECTED_DRIVER:
                return "failed";
            default:
                return null;
        }
    }
}
