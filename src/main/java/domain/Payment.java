package domain;

import java.math.BigDecimal;

public class Payment {
    private String orderId;
    private BigDecimal amount;
    private String cardType;
    private String expiry;
    private String cardholderName;
    private String cardNumber;
    private String cvc;
    //private String deliveryAddress;

    public Payment() {
        //needed for JSON binding
    }

    public Payment(String orderId, BigDecimal amount, String cardType,
                   String expiry, String cardholderName,
                   String cardNumber, String cvc) {
        this.orderId = orderId;
        this.amount = amount;
        this.cardType = cardType;
        this.expiry = expiry;
        this.cardholderName = cardholderName;
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        //this.deliveryAddress = deliveryAddress;
    }

    public String getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getCardType() { return cardType; }
    public String getExpiry() { return expiry; }
    public String getCardholderName() { return cardholderName; }
    public String getCardNumber() { return cardNumber; }
    public String getCvc() { return cvc; }
    //public String getDeliveryAddress() { return deliveryAddress; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public void setExpiry(String expiry) { this.expiry = expiry; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setCvc(String cvc) { this.cvc = cvc; }
    //public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    @Override
    public String toString() {
        return "Payment{" +
                "orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", cardType='" + cardType + '\'' +
                ", expiry='" + expiry + '\'' +
                ", cardholderName='" + cardholderName + '\'' +
                '}';
        // intentionally NOT printing cardNumber + cvc
    }
}