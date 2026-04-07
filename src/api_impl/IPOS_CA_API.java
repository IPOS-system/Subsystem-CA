package api_impl;

import api.ICatalogueAPI;
import api.ISoldStockAPI;
import domain.Product;
import domain.SaleItem;

import java.util.List;

public class IPOS_CA_API implements ISoldStockAPI, ICatalogueAPI {

	public IPOS_CA_API() {
		// TODO - implement IPOS_CA_API.IPOS_CA_API
		throw new UnsupportedOperationException();
	}

	public List<Product> listProducts() {
		// TODO - implement IPOS_CA_API.listProducts
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param keyword
	 */
	public List<Product> searchProducts(String keyword) {
		// TODO - implement IPOS_CA_API.searchProducts
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param productID
	 */
	public Product getProductDetails(String productID) {
		// TODO - implement IPOS_CA_API.getProductDetails
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param items
	 * @param orderID
	 */
	public boolean deductStock(List<SaleItem> items, String orderID) {
		// TODO - implement IPOS_CA_API.deductStock
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param productID
	 * @param qty
	 */
	public boolean checkStock(String productID, int qty) {
		// TODO - implement IPOS_CA_API.checkStock
		throw new UnsupportedOperationException();
	}

}