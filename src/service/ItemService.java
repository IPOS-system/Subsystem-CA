package service;

import dao.ItemDAO;
import domain.Item;

import java.util.List;

public class ItemService {
    private final ItemDAO itemDAO;

    public ItemService() {
        this.itemDAO = new ItemDAO(); //idk pass it in or what/
    }


    public List<Item> findAll() {
        return itemDAO.findAll();
    }

    public Item findById(String id) {
        return itemDAO.findById(id);
    }
}
