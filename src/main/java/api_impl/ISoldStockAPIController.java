package api_impl;

import api.ICatalogueAPI;
import dao.OnlineOrderDAO;
import domain.Item;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.web.bind.annotation.*;
import service.ItemService;
import service.Result;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock")
public class ISoldStockAPIController {

    private final ISoldStockAPIService service ;

    public ISoldStockAPIController(){
        service = new ISoldStockAPIService();
    }


    @PostMapping("/check")
    public Result checkProductStock(@RequestBody StockRequest request) {
        System.out.println("ipos ca checking for ID: " + request.getItemId());

        return service.checkStock(request.getItemId(), request.getQty());

    }

    @PostMapping("/deduct")
    public Result deductStock(@RequestBody StockRequest request) {
        System.out.println("ipos ca deducting for ID: " + request.getItemId() + " qty: " + request.getQty());

        return service.deductStock(request.getItemId(), request.getQty());
    }

    @PostMapping("/sendorder")
    public void sendOrder(@RequestBody Map<String, Object> request) {
        OnlineOrderDAO dao = new OnlineOrderDAO();

        String orderId = request.get("orderId").toString();
        String status = request.get("status").toString();
        String deliveryAddress = request.get("deliveryAddress").toString();
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

        boolean ok = dao.saveOnlineOrder(orderId, status, deliveryAddress, items);
        System.out.println("saved? " + ok);
    }

}

class StockRequest {
    private String itemId;
    private int qty;

    public String getItemId(){ return itemId; }
    public int getQty(){ return qty; }
}