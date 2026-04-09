package api_impl;

import api.ICatalogueAPI;
import domain.Product;

import java.util.List;

public class ICatalogueAPIService implements ICatalogueAPI {

    public List<Product> listProducts() {
        return List.of(
                new Product(),
                new Product()
        );
    }

    public List<Product> searchProducts(String keyword) {
        return listProducts();
    }

    public Product getProductDetails(String productID) {
        return new Product();
    }
}
