package IPOS_CA_DetailedModel;

public class IPOS_CA_API implements ISoldStockAPI, ICatalogueAPI {

	public IPOS_CA_API() {
		// TODO - implement IPOS_CA_API.IPOS_CA_API
		throw new UnsupportedOperationException();
	}

	public List<Products> listProducts() {
		// TODO - implement IPOS_CA_API.listProducts
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param keyword
	 */
	public List<Products> searchProducts(string keyword) {
		// TODO - implement IPOS_CA_API.searchProducts
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param productID
	 */
	public Product getProductDetails(string productID) {
		// TODO - implement IPOS_CA_API.getProductDetails
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param items
	 * @param orderID
	 */
	public boolean deductStock(List<orderItems> items, string orderID) {
		// TODO - implement IPOS_CA_API.deductStock
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param productID
	 * @param qty
	 */
	public boolean checkStock(string productID, int qty) {
		// TODO - implement IPOS_CA_API.checkStock
		throw new UnsupportedOperationException();
	}

}