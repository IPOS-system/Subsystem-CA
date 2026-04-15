package api_impl;

import api.IAccInfoAPI;
import com.fasterxml.jackson.databind.JsonNode;
import domain.LoginInfo;
import domain.OrderItem;
import service.Result;

import java.io.IOException;
import java.util.ArrayList;
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
        return saService.connect(username, password);
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
    public Result sendOrder(List<OrderItem> orderItems) {
        try {
            List<Map<String, Object>> items = new ArrayList<>();

            for (OrderItem orderItem : orderItems) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", orderItem.getItemId());   // map your itemId -> productId
                item.put("quantity", orderItem.getQuantity());
                items.add(item);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", "ord-" + System.currentTimeMillis());
            payload.put("status", "RECEIVED");
            payload.put("deliveryAddress", "test address");
            payload.put("items", items);

            System.out.println("SENDING TO SA: " + payload);

            String response = saService.post("/api/orders", payload);   // or "/sendorder" if that's the real endpoint
            System.out.println("SA RESPONSE: " + response);

            return Result.success(response);

        } catch (IOException | InterruptedException e) {
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result getAccountStatus() {
        try {
            JsonNode json = saService.get("/api/accounts/me", JsonNode.class);
            return Result.success(json.toString());   // raw JSON string
        } catch (IOException | InterruptedException e) {
            return Result.fail(e.getMessage());
        }
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

    @Override
    public Result sendCatalogue() {
        try {
            JsonNode json = saService.get("/api/catalogue", JsonNode.class);
            return Result.success(json.toString());
        } catch (IOException | InterruptedException e) {
            return Result.fail(e.getMessage());
        }
    }
}
