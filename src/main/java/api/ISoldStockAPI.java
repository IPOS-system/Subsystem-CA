package api;

import domain.SaleItem;
import service.Result;

import java.util.List;

public interface ISoldStockAPI {


	Result deductStock(String productID, int qty);


	Result checkStock(String productID, int qty);

}