package api;

import domain.Product;

import java.util.List;

public interface IOrderAPI {

	/**
	 * 
	 * @param order
	 */
	void sendOrder(List<Product> order);

	/**
	 * 
	 * @param productID
	 * @param quantity
	 */
	void removeStock(String productID, int quantity);

	/**
	 * 
	 * @param userID
	 * @param orderID
	 */
	void updateOrderHistory(int userID, String orderID);

}