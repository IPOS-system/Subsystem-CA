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
	void removeStock(string productID, int quantity);

	/**
	 * 
	 * @param userID
	 * @param orderID
	 */
	void updateOrderHistory(int userID, string orderID);

}