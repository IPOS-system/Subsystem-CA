package domain;

import java.math.BigDecimal;


public class Item {

    private String itemId;
    private String description;
    private String packageType;   // e.g. “Box”, “Bottle”
    private String unit;          // e.g. “Caps”, “Ml”
    private int    unitsInPack;   // how many units are inside a pack
    private BigDecimal packageCost; // price for ONE PACK
    private int qtyInStock;
    private int stockLimit;
    private int markup;

    public Item(String itemId, String description,String packageType, String unit, int unitsInPack,
                BigDecimal packageCost, int qtyInStock, int stockLimit, int markup){
        this.itemId = itemId;
        this.description = description;
        this.packageType =  packageType;
        this.unit = unit;
        this.unitsInPack = unitsInPack;
        this.packageCost = packageCost;
        this.qtyInStock = qtyInStock;
        this.stockLimit = stockLimit;
        this.markup = markup;
    }


    //getters and setters
    public String getItemId() {
        return itemId;
    }


    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPackageType() {
        return packageType;
    }
    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getUnitsInPack() {
        return unitsInPack;
    }
    public void setUnitsInPack(int unitsInPack) {
        this.unitsInPack = unitsInPack;
    }

    public BigDecimal getPackageCost() {
        return packageCost;
    }
    public void setPackageCost(BigDecimal packageCost) {
        this.packageCost = packageCost;
    }

    @Override
    public String toString() {
        return description + " (" + itemId + ")";
    }

    public int getQtyInStock() {
        return qtyInStock;
    }

    public int getStockLimit() {
        return stockLimit;
    }

    public int getMarkup() {
        return markup;
    }
}
