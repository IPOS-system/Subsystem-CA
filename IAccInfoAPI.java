public interface IAccInfoAPI {

	/**
	 * 
	 * @param username
	 * @param password
	 */
	void sendLoginInfo(string username, string password);

	/**
	 * 
	 * @param userID
	 */
	string getAccountStatus(int userID);

	/**
	 * 
	 * @param orderID
	 */
	string trackOrder(string orderID);

	/**
	 * 
	 * @param userID
	 */
	string getOrderHistory(int userID);

	/**
	 * 
	 * @param orderID
	 * @param status
	 */
	void updateOrderStatus(string orderID, string status);

	/**
	 * 
	 * @param orderID
	 */
	void generateInvoice(string orderID);

	/**
	 * 
	 * @param orderID
	 */
	string getInvoice(string orderID);

	/**
	 * 
	 * @param userID
	 */
	List<string> listInvoices(int userID);

	void sendCatalogue();

}