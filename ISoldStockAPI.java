public interface ISoldStockAPI {

	/**
	 * 
	 * @param items
	 * @param orderID
	 */
	boolean deductStock(List<orderItems> items, string orderID);

	/**
	 * 
	 * @param productID
	 * @param qty
	 */
	boolean checkStock(string productID, int qty);

}