package api_impl;

import api.ICatalogueAPI;
import domain.Item;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.web.bind.annotation.*;
import service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/catalogue")
public class ICatalogueController {

    private final ICatalogueAPI service ;

    public ICatalogueController(){
        service = new ICatalogueAPIService();
    }

//
//    @GetMapping("/products")
//    public List<Item> test(){
//        return service.listProducts();
//    }

    @GetMapping("/products")
    public List<Item> listProducts() {
        return service.listProducts();
    }


}