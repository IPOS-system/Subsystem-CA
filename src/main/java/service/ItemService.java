package service;

import dao.ItemDAO;
import domain.Item;
import org.apache.logging.log4j.message.ReusableMessage;

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

    public Result addItemToStock(Item i) {
        if (itemDAO.findById(i.getItemId()) != null) {
            return Result.fail("item ID already exists");
        }

        if (itemDAO.addItemToStock(i)) {
            return Result.success("item added to stock successfully");
        }
        return Result.fail("failure");
    }

    public Result modifyQtyInStock(String itemId, int newQty){
        if(itemDAO.modifyQtyInStock(itemId, newQty)){
            return Result.success("stock qty update successfully");
        }
        return Result.fail("failure");
    }


    public Result removeItemFromStock(String itemId){
        Item item = itemDAO.findById(itemId);

        if(item != null && itemDAO.findById(itemId).getQtyInStock() ==0){
            if(itemDAO.removeItemFromStock(itemId)){
                return Result.success("item delete successfully");
            }
            else{
                return Result.fail("item delete fail");
            }
        }
        else{
            return Result.fail("qty is NOT zero. so not deleted. ");
        }


    }


}
