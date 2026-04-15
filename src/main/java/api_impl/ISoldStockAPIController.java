package api_impl;

import api.ICatalogueAPI;
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
        System.out.println("FULL BODY: " + request);
        System.out.println("orderId: " + request.get("orderId"));
        System.out.println("status: " + request.get("status"));
        System.out.println("deliveryAddress: " + request.get("deliveryAddress"));
        System.out.println("items: " + request.get("items"));
    }

}

class StockRequest {
    private String itemId;
    private int qty;

    public String getItemId(){ return itemId; }
    public int getQty(){ return qty; }
}