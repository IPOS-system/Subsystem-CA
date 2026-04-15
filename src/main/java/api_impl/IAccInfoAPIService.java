package api_impl;

import api.IAccInfoAPI;
import domain.LoginInfo;
import domain.OrderItem;
import service.Result;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IAccInfoAPIService implements IAccInfoAPI {
    private final SAService saService;

    public IAccInfoAPIService(SAService saService){
        this.saService = saService;
    }



    //post
    public Result connect(String username, String password) {

        if( saService.connect(username, password)){
            return Result.success("coonnect");
        }
        return Result.fail("failed to connect. ");

    }

    public boolean isConnected (){
        return saService.isConnected();
    }

    @Override
    public LoginInfo sendLoginInfo(String username, String password) {
        return null;
    }

    //
    @Override
    public Result sendOrder(List<OrderItem> orderItems){
        return Result.fail("not done yet");
    }

    //get
    @Override
    public String getAccountStatus(int userID) {
        return "";
    }

    //get
    @Override
    public String trackOrder(String orderID) {
        return "";
    }

    //get
    @Override
    public String getOrderHistory(int userID) {
        return "";
    }

    //put
    @Override
    public void updateOrderStatus(String orderID, String status) {

    }

    //get
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

    //get
    @Override
    public void sendCatalogue() {

    }
}
