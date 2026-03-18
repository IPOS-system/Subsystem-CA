public interface ICatalogueAPI {

	List<Product> listProducts();

	/**
	 * 
	 * @param keyword
	 */
	List<Product> searchProducts(string keyword);

	/**
	 * 
	 * @param productID
	 */
	Product getProductDetails(string productID);

}