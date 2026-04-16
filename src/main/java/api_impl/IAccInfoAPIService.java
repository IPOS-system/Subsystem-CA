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

    public int getMerchantId(){
        return saService.getMerchantId();
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
            payload.put("deliveryAddress", "35 northampton square NW2343");
            payload.put("items", items);

            System.out.println("SENDING TO SA: " + payload);

            String response = saService.post("/api/orders", payload);   // or "/sendorder" if that's the real endpoint
            System.out.println("SA RESPONSE: " + response);

            return Result.success(response);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("failed to send order to IPOS SA... \n check system status...");
        }
    }


    @Override
    public Result getAccountStatus() {
        try {
            JsonNode json = saService.get("/api/accounts/me", JsonNode.class);

            String text =
                    "merchantId -> " + json.path("merchantId").asText() + "\n" +
                            "userId -> " + json.path("userId").asText() + "\n" +
                            "username -> " + json.path("username").asText() + "\n" +
                            "companyName -> " + json.path("companyName").asText() + "\n" +
                            "address -> " + json.path("address").asText() + "\n" +
                            "phone -> " + json.path("phone").asText() + "\n" +
                            "fax -> " + json.path("fax").asText() + "\n" +
                            "email -> " + json.path("email").asText() + "\n" +
                            "creditLimit -> " + json.path("creditLimit").asText() + "\n" +
                            "currentBalance -> " + json.path("currentBalance").asText() + "\n" +
                            "accountStatus -> " + json.path("accountStatus").asText() + "\n" ;

            return Result.success(text);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("ipos SA offline...\n account status unavailable. ");

        }
    }

    //get
    @Override
    public String trackOrder(String orderID) {
        return "";
    }

    @Override
    public Result getOrderHistory(int merchantId) {
        try {
            JsonNode json = saService.get("/api/orders/merchant/" + merchantId, JsonNode.class);
            return Result.success(json.toString());   //raw JSON array
        } catch (Exception e) {
            return Result.fail(e.toString());
        }
    }

    //put
    @Override
    public Result updateOrderStatus(String orderID, String status) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("status", status);

            System.out.println("updateOrderStatus called");
            System.out.println("orderID = " + orderID);
            System.out.println("status = " + status);

            String response = saService.put("/api/orders/" + orderID + "/status", payload);

            System.out.println("final response = " + response);
            return Result.success(response);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(e.toString());
        }
    }
    //get
    @Override
    public void generateInvoice(String orderID) {

    }
    @Override
    public Result getInvoice(String invoiceId) {
        try {
            JsonNode json = saService.get("/api/invoices/" + invoiceId, JsonNode.class);
            return Result.success(json.toString());
        } catch (Exception e) {
            return Result.fail(e.toString());
        }
    }

    @Override
    public Result listInvoices(int merchantId) {
        try {
            JsonNode json = saService.get("/api/invoices/merchant/" + merchantId, JsonNode.class);
            return Result.success(json.toString());
        } catch (Exception e) {
            return Result.fail(e.toString());
        }
    }

    @Override
    public Result sendCatalogue() {
        try {
            JsonNode json = saService.get("/api/catalogue", JsonNode.class);
            return Result.success(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }
    }
}
