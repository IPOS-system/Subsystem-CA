package api_impl;

import api.ICatalogueAPI;
import domain.Item;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.springframework.stereotype.Service;
import service.ItemService;

import java.util.List;

public class ICatalogueAPIService implements ICatalogueAPI {
    private ItemService itemService= new ItemService();

    public ICatalogueAPIService() {
    }

    @Override
    public List<Item> listProducts() {

        return itemService.findAll();
    }


}
