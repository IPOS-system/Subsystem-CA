package service;

import domain.Item;
import domain.SaleItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleService {

    private final List<SaleItem> basket = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    public Result addItemToBasket(Item item, int qty) {
        if (item == null){
            return Result.fail("no item brah");
        }
        else if (qty <= 0) {
            return Result.fail("Invalid quantity");
        }

        else {
            //preventing duplicate sale items in the basekt (will break sql)
            for (SaleItem line : basket) {
                if (line.getItemId().equals(item.getItemId())) {
                    line.updateQuantity(line.getQuantity() + qty);
                    return Result.success("added (updated quantity)");
                }
            }
            basket.add(new SaleItem(item.getItemId(), item.getDescription(), qty, item.getPackageCost()));
           return Result.success("added");
        }
    }

    public BigDecimal getBasketTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (SaleItem line : basket) {
            total = total.add(line.getOrderItemPrice());
        }

        return total;
    }

    public Result removeItemFromBasket(String itemId) {

        for (SaleItem line : basket) {
            if (line.getItemId().equals(itemId)) {
                basket.remove(line);
                return Result.success("Item removed");
            }
        }

        return Result.fail("Item not found in basket");
    }

    public List<SaleItem> getBasket() {
        return basket;
    }

    public void clearBasket(){
        basket.clear();

    }


    //for orders with SA
    public Result placeOrder(){
        basket.clear();
        return Result.fail("not done yet");
    }

    //for sales to customers in store.
    public Result placeSale(){
        basket.clear();
        return Result.fail("not done yet");

    };
}
