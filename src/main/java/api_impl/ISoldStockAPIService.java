package api_impl;

import api.ISoldStockAPI;
import domain.SaleItem;
import service.ItemService;
import service.Result;

import java.util.List;

public class ISoldStockAPIService implements ISoldStockAPI {
     private ItemService itemService= new ItemService();

     @Override
    public Result checkStock(String productID, int qty){
        return itemService.checkStock(productID, qty);
    }
    @Override
    public 	Result deductStock(String productID, int qty){
        return itemService.deductStock(productID, qty);
    }

    public Result sendOrder(){
         return Result.fail("FAIL");
    }
}
