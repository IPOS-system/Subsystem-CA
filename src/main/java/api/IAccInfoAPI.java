package api;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import domain.OrderItem;
import service.Result;

import java.util.List;

public interface IAccInfoAPI {

	/**
	 * 
	 * @param username
	 * @param password
	 */
	void sendLoginInfo(String username, String password);

	Result sendOrder(List<OrderItem> orderItems);


	/**
	 * 
	 * @param userID
	 */
	String getAccountStatus(int userID);

	/**
	 * 
	 * @param orderID
	 */
	String trackOrder(String orderID);

	/**
	 * 
	 * @param userID
	 */
	String getOrderHistory(int userID);

	/**
	 * 
	 * @param orderID
	 * @param status
	 */
	void updateOrderStatus(String orderID, String status);

	/**
	 * 
	 * @param orderID
	 */
	void generateInvoice(String orderID);

	/**
	 * 
	 * @param orderID
	 */
	String getInvoice(String orderID);

	/**
	 * 
	 * @param userID
	 */
	List<String> listInvoices(int userID);

	void sendCatalogue();

}