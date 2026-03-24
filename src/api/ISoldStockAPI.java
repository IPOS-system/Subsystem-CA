package api;

import domain.orderItems;

import java.util.List;

public interface ISoldStockAPI {

	/**
	 * 
	 * @param items
	 * @param orderID
	 */
	boolean deductStock(List<orderItems> items, String orderID);

	/**
	 * 
	 * @param productID
	 * @param qty
	 */
	boolean checkStock(String productID, int qty);

}