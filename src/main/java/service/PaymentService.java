package service;


import domain.CustomerAccount;
import domain.PaymentInfo;

import java.math.BigDecimal;

//handles payments for occasional customers, also handles acc holders buying things and making repayments/
public class PaymentService {
    private final DebtService debtService;

    public PaymentService(DebtService debtService){
        this.debtService = debtService;
    }


    public Result validatePayment(CustomerAccount customer, BigDecimal total, PaymentInfo paymentMethod) {

        //check account status,

        if("account".equals(paymentMethod.method)) {
            if(customer == null){
                return Result.fail("no customer account for account payment");
            }

            else if (!("active".equals(customer.getAccountStatus()))) {
                return Result.fail("customer " + customer.getAccountHolderName() +
                        " is currently " + customer.getAccountStatus() + ". please fix account status and try again. order not placed");
            }

            BigDecimal outstanding = debtService.getOutstandingDebt(customer.getAccountId());
            BigDecimal newTotalDebt = outstanding.add(total);

            if (newTotalDebt.compareTo(customer.getCreditLimit()) > 0) {
                return Result.fail("order total " + total + " exceeds customer " +
                        customer.getAccountHolderName() +
                        " available credit. limit: " + customer.getCreditLimit());
            }
        }



        else if("card".equals(paymentMethod.method)){
            //call pu. but probably not here, once transaction has succeeded
        }
        //card and cash always succeed, so now put entries in table.

    return Result.success("payment validated ");
    }

    //public Result makeDebtRepayment()

}
