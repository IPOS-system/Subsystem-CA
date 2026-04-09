package api;

import domain.Product;

import java.util.List;

public interface ICatalogueAPI {

	List<Product> listProducts();

	/**
	 * 
	 * @param keyword
	 */
	List<Product> searchProducts(String keyword);

	/**
	 * 
	 * @param productID
	 */
	Product getProductDetails(String productID);

}