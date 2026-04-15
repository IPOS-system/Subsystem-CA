package domain;

import java.math.BigDecimal;

public class OrderItem {

    private String itemId;
    //private String description;
    private int quantity;
    //private BigDecimal unitCost;
    //private BigDecimal total;

    public OrderItem() {
    }

    public OrderItem(String itemId, int quantity
    ) {
        this.itemId = itemId;
        //this.description = description;
        this.quantity = quantity;
        //this.unitCost = unitCost;
        //this.total = total;
    }

    public String getItemId() {
        return itemId;
    }

//    public String getDescription() {
//        return description;
//    }

    public int getQuantity() {
        return quantity;
    }

    //public BigDecimal getUnitCost() {
//        return unitCost;
//    }
//
//    public BigDecimal getTotal() {
//        return total;
//    }
//}
}