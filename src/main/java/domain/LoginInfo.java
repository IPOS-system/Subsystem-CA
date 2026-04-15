package domain;

public class LoginInfo {
    private int userId;
    private String username;
    private String role;
    private int merchantId;
    private boolean paymentReminderDue;
    private String token;

    public LoginInfo() {
    }

    public LoginInfo(int userId, String username, String role,
                     int merchantId, boolean paymentReminderDue, String token) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.merchantId = merchantId;
        this.paymentReminderDue = paymentReminderDue;
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public boolean isPaymentReminderDue() {
        return paymentReminderDue;
    }

    public void setPaymentReminderDue(boolean paymentReminderDue) {
        this.paymentReminderDue = paymentReminderDue;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}