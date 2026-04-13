package service;

import dao.CatalogueDAO;
import domain.Item;

import java.util.List;

public class CatalogueService {
    private final CatalogueDAO catalogueDAO;
    public CatalogueService(){
        this.catalogueDAO = new CatalogueDAO();
    }

    public List<Item> findAll() {
        return catalogueDAO.findAll();
    }

    public Item findById(String itemId) {
        return catalogueDAO.findById(itemId);
    }
}
