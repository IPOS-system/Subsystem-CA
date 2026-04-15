package service;

import dao.CustomerAccountDAO;
import dao.DiscountPlanDAO;
import domain.CustomerAccount;
import domain.DebtRecord;
import domain.DiscountPlan;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    private final CustomerAccountDAO customerDao;
    private final DiscountPlanDAO discountPlanDAO;
    private final DebtService debtService;
    private final ReminderService reminderService;



    public CustomerService(DebtService debtService) {
        this.customerDao = new CustomerAccountDAO();
        this.discountPlanDAO = new DiscountPlanDAO();
        this.debtService = debtService;
        this.reminderService = new ReminderService();
    }

    public BigDecimal getOutstandingDebt(String id){
        return debtService.getOutstandingDebt(id);
    }

    public CustomerAccount findById(String id) {
        return customerDao.findById(id);
    }

    public DiscountPlan getCustomerDiscountPlan (String customerId){
        try{
            return discountPlanDAO.getCustomerDiscountPlan(customerId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Result> generatereminder(String firstOrSecond, String accountId, String holderName){
        String firstOrSecondMsg;

        List<DebtRecord> allDebtRecords = debtService.getAllOutstandingDebts(accountId);
        List<DebtRecord> debtToGenerateReminderOn = new ArrayList<>();

        if("F".equals(firstOrSecond)){
            firstOrSecondMsg = "F";
            for(DebtRecord d : allDebtRecords){
                if ("due".equals(d.getFirstReminder())) {
                    debtToGenerateReminderOn.add(d);
                }
            }
            if(debtToGenerateReminderOn.isEmpty()){
                return List.of(Result.fail("no debts with first reminder due"));
            }
        }

        else {
            firstOrSecondMsg = "THIS IS THE SECOND REMINDER";
            for (DebtRecord d : allDebtRecords) {
                if ("due".equals(d.getSecondReminder())) {
                    debtToGenerateReminderOn.add(d);
                }
            }
            if (debtToGenerateReminderOn.isEmpty()) {
                return List.of(Result.fail("no debts with second reminder due..."));
            }
        }

        List<Result> returnRes = new ArrayList<>();
        for(DebtRecord d : debtToGenerateReminderOn){
            try{
                returnRes.add(
                        Result.success(reminderService.buildOverdueReminder(firstOrSecondMsg, accountId, holderName, d))
                );
            } catch (IOException e) {
                e.printStackTrace();
                returnRes.add(Result.fail("failure"));
            }
        }
        return returnRes;
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


}