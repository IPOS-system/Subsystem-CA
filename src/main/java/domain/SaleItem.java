package domain;

import java.math.BigDecimal;

public class SaleItem {


    private  String saleId;
    private final String itemId;



    private final String itemDescription;
    private  int quantity; //mutable
    private final BigDecimal unitPrice;


    public SaleItem(String itemId, String itemDescription, int quantity, BigDecimal unitPrice) {
        //this.saleId = saleId;
        this.itemId = itemId;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;


    }

    public String getItemId() {
        return itemId;
    }
    public String getItemDescription() {
        return itemDescription;
    }
    public int getQuantity() {
        return quantity;
    }
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    public BigDecimal getOrderItemPrice() {
        return unitPrice.multiply( BigDecimal.valueOf(quantity));
    }

    public void setSaleId(String saleId){
        this.saleId = saleId;
    }


    public void updateQuantity(int quantity) {
        this.quantity = quantity;

    }

}
