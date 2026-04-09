package domain;

import java.math.BigDecimal;


public class Item {

    private String itemId;
    private String description;
    private String packageType;   // e.g. “Box”, “Bottle”
    private String unit;          // e.g. “Caps”, “Ml”
    private int    unitsInPack;   // how many units are inside a pack
    private BigDecimal packageCost; // price for ONE PACK

    //getters and setters
    public String getItemId() {
        return itemId;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
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
}
