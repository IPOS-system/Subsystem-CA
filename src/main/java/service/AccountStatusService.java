package service;

import dao.CustomerAccountDAO;
import dao.DebtsDAO;

import java.time.LocalDate;

public class AccountStatusService {

    private final DebtsDAO debtsDAO;
    private final CustomerAccountDAO customerAccountDAO;

    public AccountStatusService() {

        this.customerAccountDAO = new CustomerAccountDAO();
        this.debtsDAO = new DebtsDAO();
    }

    public void refreshStatuses(LocalDate appDate) {
        debtsDAO.applyFirstReminders(appDate);
        debtsDAO.applySecondReminders(appDate);

       customerAccountDAO.markAccountsInDefault();
        customerAccountDAO.markAccountsSuspended();
        customerAccountDAO.markAccountsActiveIfClear();
    }
}