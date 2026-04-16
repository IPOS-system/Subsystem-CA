package api;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import domain.LoginInfo;
import domain.OrderItem;
import service.Result;

import java.util.List;

public interface IAccInfoAPI {

	/**
	 * 
	 * @param username
	 * @param password
	 */
	LoginInfo sendLoginInfo(String username, String password);

	Result sendOrder(List<OrderItem> orderItems);



	Result getAccountStatus();

	/**
	 * 
	 * @param orderID
	 */
	String trackOrder(String orderID);

	/**
	 * 
	 * @param userID
	 */
	Result getOrderHistory(int userID);

	/**
	 * 
	 * @param orderID
	 * @param status
	 */
	Result updateOrderStatus(String orderID, String status);

	/**
	 * 
	 * @param orderID
	 */
	void generateInvoice(String orderID);

	/**
	 * 
	 * @param orderID
	 */
	Result getInvoice(String orderID);

	/**
	 * 
	 * @param userID
	 */
	Result listInvoices(int userID);

	Result sendCatalogue();

}