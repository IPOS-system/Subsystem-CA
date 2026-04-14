package service;

import dao.DebtsDAO;
import domain.DebtRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class DebtService {
    private final DebtsDAO debtDAO;
    private final TimeService timeService;
    private final AccountStatusService accountStatusService;
    public DebtService(TimeService timeService, AccountStatusService accountStatusService){
        this.debtDAO = new DebtsDAO();
        this.timeService = timeService;
        this.accountStatusService = accountStatusService;
    }

    public BigDecimal getOutstandingDebt(String accountId){
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        for (DebtRecord d : debtDAO.getDebts(accountId)){
            totalOutstanding = totalOutstanding.add(d.getRemaining());
        }
        return totalOutstanding;
    }

    //if its there return id, if not create and then return id
    public int recordSaleDebt(Connection con, String accountId, BigDecimal amount) {
        Date month = Date.valueOf(timeService.today().withDayOfMonth(1)); //first
        Date dueDate = null;

        DebtRecord debt = debtDAO.getCurrentMonthDebt(con, accountId, month);

        if (debt == null) {
            return debtDAO.createDebt(con, accountId, month, dueDate, amount);
        } else {
            debtDAO.addToDebt(con, debt.getDebtId(), amount);
            return debt.getDebtId();
        }
    }



    public Result applyPayment(String accountId, BigDecimal paymentAmount) {

        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail("invalid payment amount");
        }

        List<DebtRecord> debts = debtDAO.getDebts(accountId);

        if (debts.isEmpty()) {
            return Result.fail("no outstanding debts");
        }

        BigDecimal remainingPayment = paymentAmount;

        for (DebtRecord debt : debts) {

            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal debtRemaining = debt.getRemaining();

            if (debtRemaining.compareTo(remainingPayment) <= 0) {
                debtDAO.updateRemaining(debt.getDebtId(), BigDecimal.ZERO);
                remainingPayment = remainingPayment.subtract(debtRemaining);
            } else {
                BigDecimal newRemaining = debtRemaining.subtract(remainingPayment);
                debtDAO.updateRemaining(debt.getDebtId(), newRemaining);
                remainingPayment = BigDecimal.ZERO;
            }
        }
        accountStatusService.refreshStatuses(timeService.today());
        return Result.success("payment applied");
    }

}
