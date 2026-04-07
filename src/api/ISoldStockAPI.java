package api;

import domain.SaleItem;

import java.util.List;

public interface ISoldStockAPI {

	/**
	 * 
	 * @param items
	 * @param orderID
	 */
	boolean deductStock(List<SaleItem> items, String orderID);

	/**
	 * 
	 * @param productID
	 * @param qty
	 */
	boolean checkStock(String productID, int qty);

}