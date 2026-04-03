package domain;

import java.math.BigDecimal;

public class CustomerAccount {

    private String accountId;
    private String accountHolderName;
    private String contactName;
    private String address;
    private String phone;
    private BigDecimal creditLimit;
    private Integer discount_id;       //integer, not int, because DB can store NULL
    private String discountPlanType;   // fixed / tiered
    private String accountStatus;

    public CustomerAccount() {
    }

    public CustomerAccount(String accountId, String accountHolderName, String contactName,
                           String address, String phone, BigDecimal creditLimit,
                           Integer discount_id, String discountPlanType, String accountStatus) {
        this.accountId = accountId;
        this.accountHolderName = accountHolderName;
        this.contactName = contactName;
        this.address = address;
        this.phone = phone;
        this.creditLimit = creditLimit;
        this.discount_id = discount_id;
        this.discountPlanType = discountPlanType;
        this.accountStatus = accountStatus;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Integer getAgreedDiscountId() {
        return discount_id;
    }

    public void setAgreedDiscountId(Integer discount_id) {
        this.discount_id = discount_id;
    }

    public String getDiscountPlanType() {
        return discountPlanType;
    }

    public void setDiscountPlanType(String discountPlanType) {
        this.discountPlanType = discountPlanType;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}