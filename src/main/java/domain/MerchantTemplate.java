package domain;


public class MerchantTemplate {

    private String pharmacyName;
    private String address;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private String phone;
    private String email;

    // full templates as text
    private String receiptTemplate;
    private String reminderTemplate;
    private String invoiceTemplate;

    public String getReceiptTemplate() {
        return receiptTemplate;
    }

    public void setReceiptTemplate(String receiptTemplate) {
        this.receiptTemplate = receiptTemplate;
    }

    public String getReminderTemplate() {
        return reminderTemplate;
    }

    public void setReminderTemplate(String reminderTemplate) {
        this.reminderTemplate = reminderTemplate;
    }

    public String getInvoiceTemplate() {
        return invoiceTemplate;
    }

    public void setInvoiceTemplate(String invoiceTemplate) {
        this.invoiceTemplate = invoiceTemplate;
    }

    // getters/setters
}