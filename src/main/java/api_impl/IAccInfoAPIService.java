package api_impl;

import api.IAccInfoAPI;
import domain.OrderItem;
import service.Result;

import java.util.List;

public class IAccInfoAPIService implements IAccInfoAPI {
    @Override
    public void sendLoginInfo(String username, String password) {

    }

    public Result sendOrder(List<OrderItem> orderItems){
        return Result.fail("not done yet");
    }


    @Override
    public String getAccountStatus(int userID) {
        return "";
    }

    @Override
    public String trackOrder(String orderID) {
        return "";
    }

    @Override
    public String getOrderHistory(int userID) {
        return "";
    }

    @Override
    public void updateOrderStatus(String orderID, String status) {

    }

    @Override
    public void generateInvoice(String orderID) {

    }

    @Override
    public String getInvoice(String orderID) {
        return "";
    }

    @Override
    public List<String> listInvoices(int userID) {
        return List.of();
    }

    @Override
    public void sendCatalogue() {

    }
}
