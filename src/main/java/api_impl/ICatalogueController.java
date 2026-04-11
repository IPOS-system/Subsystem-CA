package api_impl;

import api.ICatalogueAPI;
import domain.Product;

import domain.SaleItem;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/catalogue")
public class ICatalogueController {

    private final ICatalogueAPI service = new ICatalogueAPIService();

    @GetMapping("/test")
    public SaleItem test(){
        SaleItem test= new SaleItem("001", "test", 5, BigDecimal.valueOf(4));

        System.out.println("test was called in cataloge api. ");
        return test;
    }

    @GetMapping("/products")
    public List<Product> listProducts() {
        return service.listProducts();
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam String keyword) {
        return service.searchProducts(keyword);
    }

    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable String id) {
        return service.getProductDetails(id);
    }
}