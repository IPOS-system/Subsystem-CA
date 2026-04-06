package service;

import dao.CustomerAccountDAO;
import domain.CustomerAccount;

import java.math.BigDecimal;
import java.util.List;

public class CustomerService {

    private final CustomerAccountDAO customerDao;

    public CustomerService() {
        this.customerDao = new CustomerAccountDAO();
    }

    public CustomerAccount findById(String id) {
        return customerDao.findById(id);
    }

    public Result createCustomerAccount(String accountId, String holderName, String contactName,
                                        String address, String phone, String creditText, String status) {
        accountId = accountId.trim();
        holderName = holderName.trim();
        contactName = contactName.trim();
        address = address.trim();
        phone = phone.trim();
        creditText = creditText.trim();

        if (accountId.isEmpty() || holderName.isEmpty() || creditText.isEmpty()) {
            return Result.fail("account id, holder name and credit limit required.");
        }

        BigDecimal creditLimit;
        try {
            creditLimit = new BigDecimal(creditText);
        } catch (NumberFormatException ex) {
            return Result.fail("credit limit must be a valid number.");
        }

        CustomerAccount c = new CustomerAccount();
        c.setAccountId(accountId);
        c.setAccountHolderName(holderName);
        c.setContactName(contactName);
        c.setAddress(address);
        c.setPhone(phone);
        c.setCreditLimit(creditLimit);
        c.setAgreedDiscountId(null);
        c.setAccountStatus(status);

        boolean ok = customerDao.createCustomerAccount(c);

        if (!ok) {
            return Result.fail("could not create customer account.");
        }

        return Result.success("customer account created.");
    }

    public Result deleteCustomerAccount(String id) {
        boolean ok = customerDao.deleteCustomerAccount(id);
        if(!ok) {
            return Result.fail("could not delete customer account.");
        }
        return Result.success("customer account deleted.");
    }

    public Result updateCustomerAccount(String accountId, String holderName, String contactName,
                                        String address, String phone, String creditText, String status) {
        accountId = accountId.trim();
        holderName = holderName.trim();
        contactName = contactName.trim();
        address = address.trim();
        phone = phone.trim();
        creditText = creditText.trim();


        if (accountId.isEmpty()) {
            return Result.fail("account id is empty.");
        }

        if (holderName.isEmpty() || creditText.isEmpty()) {
            return Result.fail("account holder name and credit limit required. ");
        }

        BigDecimal creditLimit;

        try {
            creditLimit = new BigDecimal(creditText);
        } catch (NumberFormatException ex) {
            return Result.fail("credit limit must be a valid number.");
        }

        //build account object after check passed.
        CustomerAccount c = new CustomerAccount();
        c.setAccountId(accountId);
        c.setAccountHolderName(holderName);
        c.setContactName(contactName);
        c.setAddress(address);
        c.setPhone(phone);
        c.setCreditLimit(creditLimit);
        c.setAccountStatus(status);
        if( customerDao.updateCustomerAccount(c)){
            return Result.success("customer account updated.");
        }
        else{
            return Result.fail("could not update customer account.");
        }
    }

    public List<CustomerAccount> getAllCustomerAccounts() {
        return customerDao.getAllCustomerAccounts();
    }

    //a helper class to return success or fail messages to the GUI.
    public static class Result {
        private final boolean success;
        private final String message;

        private Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static Result success(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}