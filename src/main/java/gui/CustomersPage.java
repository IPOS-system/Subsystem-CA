package gui;


import domain.CustomerAccount;
import service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import java.math.BigDecimal;
import java.util.List;

//this is refactored into customerspage and customerservice.

public class CustomersPage extends JPanel {

    private final AppController appController;
    private final CustomerService customerService;

    private JTable tbl;
    private DefaultTableModel model;

    private JTextField accountIdTxt;
    private JTextField holderNameTxt;
    private JTextField contactNameTxt;
    private JTextField addressTxt;
    private JTextField phoneTxt;
    private JTextField creditLimitTxt;
    private JTextField agreedDiscountTxt;   //shows fixed/tiered
    private JComboBox<String> statusDrop;

    private JButton addBtn;
    private JButton delBtn;
    private JButton clrBtn;
    private JButton genFirstReminder;
    private JButton genSecondReminder;

    private JButton makePaymentBtn;

    private JButton updBtn;
    private JButton discountBtn;

    private PaymentService paymentService;

    public CustomersPage(AppController appController, CustomerService customerService, PaymentService paymentService) {
        this.appController = appController;
        this.customerService = customerService;

        this.paymentService = paymentService;

        setLayout(new BorderLayout());

        add(new HeaderPanel(this.appController), BorderLayout.NORTH);
        add(makeMiddle(), BorderLayout.CENTER);
        add(new BottomPanel(this.appController), BorderLayout.SOUTH);

        loadCustomersIntoTable();
        hookEvents();
    }

    private JPanel makeMiddle() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setOpaque(false);

        model = new DefaultTableModel(
                new Object[]{
                        "Account ID",
                        "Holder Name",
                        "Contact",
                        "Phone",
                        "Credit Limit",
                        "Discount Plan",
                        "Status",
                        "Balance"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tbl = new JTable(model);
        JScrollPane sp = new JScrollPane(tbl);

        JPanel form = new JPanel(new GridLayout(0, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Manage customer accounts"));

        accountIdTxt = new JTextField();
        holderNameTxt = new JTextField();
        contactNameTxt = new JTextField();
        addressTxt = new JTextField();
        phoneTxt = new JTextField();
        creditLimitTxt = new JTextField();
        agreedDiscountTxt = new JTextField();
        agreedDiscountTxt.setEditable(false); // display only

        statusDrop = new JComboBox<>(new String[]{
                "Active",
                "Inactive"
        });

        addBtn = new JButton("Add Account");
        delBtn = new JButton("Delete Account");
        clrBtn = new JButton("Deselect Account");
        genFirstReminder = new JButton("Generate 1st Reminder");
        genSecondReminder = new JButton("Generate 2nd Reminder");
        makePaymentBtn = new JButton("Make Payment");
        updBtn = new JButton("Update Details");
        discountBtn = new JButton("View / Edit Discount Plans");

        form.add(new JLabel("Account ID:"));
        form.add(accountIdTxt);
        form.add(new JLabel("Holder Name:"));
        form.add(holderNameTxt);
        form.add(new JLabel("Contact Name:"));
        form.add(contactNameTxt);
        form.add(new JLabel("Address:"));
        form.add(addressTxt);
        form.add(new JLabel("Phone:"));
        form.add(phoneTxt);
        form.add(new JLabel("Credit Limit:"));
        form.add(creditLimitTxt);
        form.add(new JLabel("Agreed Discount:"));
        form.add(agreedDiscountTxt);
        form.add(new JLabel("Status:"));
        form.add(statusDrop);

        form.add(addBtn);
        form.add(delBtn);
        form.add(updBtn);
        form.add(clrBtn);
        form.add(discountBtn);
        form.add(genFirstReminder);
        form.add(genSecondReminder);
        form.add(makePaymentBtn);

        form.add(new JLabel(""));
        form.add(new JLabel(""));
        form.add(new JLabel(""));

        p.add(sp, BorderLayout.CENTER);
        p.add(form, BorderLayout.SOUTH);

        return p;
    }



    private void hookEvents() {
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            int row = tbl.getSelectedRow();

            if (row != -1) {
                String accountId = model.getValueAt(row, 0).toString();

                //CustomerAccount c = customerDao.findById(accountId);
                CustomerAccount c = customerService.findById(accountId);

                if (c != null) {
                    accountIdTxt.setText(c.getAccountId());
                    holderNameTxt.setText(c.getAccountHolderName());
                    contactNameTxt.setText(c.getContactName());
                    addressTxt.setText(c.getAddress());
                    phoneTxt.setText(c.getPhone());
                    creditLimitTxt.setText(c.getCreditLimit() != null ? c.getCreditLimit().toString() : "");
                    agreedDiscountTxt.setText(c.getDiscountPlanType() != null ? c.getDiscountPlanType() : "");
                    statusDrop.setSelectedItem(c.getAccountStatus());

                    accountIdTxt.setEditable(false);
                }
            }
        });

        //add customer button.
        addBtn.addActionListener(e -> {
            Result result = customerService.createCustomerAccount(
                    accountIdTxt.getText(),
                    holderNameTxt.getText(),
                    contactNameTxt.getText(),
                    addressTxt.getText(),
                    phoneTxt.getText(),
                    creditLimitTxt.getText(),
                    statusDrop.getSelectedItem().toString()
            );

            JOptionPane.showMessageDialog(this, result.getMessage());

            if (result.isSuccess()) {
                resetForm();
                loadCustomersIntoTable();
            }
        });

        delBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select account first.");
                return;
            }

            String accountId = accountIdTxt.getText().trim();

            int yesNo = JOptionPane.showConfirmDialog(
                    this,
                    "Delete account: " + accountId + "?",
                    "Confirm Account Deletion",
                    JOptionPane.YES_NO_OPTION
            );

            if (yesNo == JOptionPane.YES_OPTION) {
                Result result = customerService.deleteCustomerAccount(accountId);

                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, result.getMessage());
                    resetForm();
                    loadCustomersIntoTable();
                } else {
                    JOptionPane.showMessageDialog(this, result.getMessage());
                }
            }
        });


        genFirstReminder.addActionListener(e -> {
            if(tbl.getSelectedRow() == -1){
                JOptionPane.showMessageDialog(this, "Select a customer first.");
            }
            List<Result> results = customerService.generatereminder(
                    "F",
                    accountIdTxt.getText(),
                    holderNameTxt.getText()
            );
            for (Result result : results) {
                JOptionPane.showMessageDialog(this, result.getMessage());
            }
        });

        genSecondReminder.addActionListener(e -> {
            if(tbl.getSelectedRow() == -1){
                JOptionPane.showMessageDialog(this, "Select a customer first.");
            }
            List<Result> results = customerService.generatereminder(
                    "S",
                    accountIdTxt.getText(),
                    holderNameTxt.getText()
            );
            for (Result result : results) {
                JOptionPane.showMessageDialog(this, result.getMessage());
            }
        });

        clrBtn.addActionListener(e -> resetForm());

        updBtn.addActionListener(e -> {
            Result result = customerService.updateCustomerAccount( accountIdTxt.getText(),
            holderNameTxt.getText(),
             contactNameTxt.getText(),
           addressTxt.getText().trim(),
            phoneTxt.getText().trim(),
            creditLimitTxt.getText().trim(),
           statusDrop.getSelectedItem().toString());


            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage());
                resetForm();
                loadCustomersIntoTable();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage());
            }
        });

        makePaymentBtn.addActionListener(e -> {
            CustomerAccount customer = customerService.findById(accountIdTxt.getText());
            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Select a customer first.");
                return;
            }

            String input = JOptionPane.showInputDialog(this,
                    "Enter repayment amount for " + customer.getAccountHolderName() + ":");

            if (input == null) {
                return; //user cancelled
            }

            //if account in default, manager requred to fix it.
            if("in default".equals(customer.getAccountStatus()) && !("Director of Operations/Manager".equals(appController.getCurrentUser().getRole()))){

                JOptionPane.showMessageDialog(this, "Manager action required");
                return;
            }

            try {
                BigDecimal amount = new BigDecimal(input.trim());

                Result result = paymentService.makeDebtRepayment(customer, amount);

                JOptionPane.showMessageDialog(this, result.getMessage());

                if (result.isSuccess()) {
                    loadCustomersIntoTable();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount.");
            }
        });

        discountBtn.addActionListener(e ->{
            String accountId = accountIdTxt.getText();
            if(accountId.isEmpty()){
                JOptionPane.showMessageDialog(this, "Select a customer first.");
                return;
            }
            appController.showDiscountPlanPage(accountId);
        }
        );
    }

    private void loadCustomersIntoTable() {
        model.setRowCount(0);

        List<CustomerAccount> allCustomers = customerService.getAllCustomerAccounts();

        for (CustomerAccount c : allCustomers) {
            model.addRow(new Object[]{
                    c.getAccountId(),
                    c.getAccountHolderName(),
                    c.getContactName(),
                    c.getPhone(),
                    c.getCreditLimit(),
                    c.getDiscountPlanType(),
                    c.getAccountStatus(),
                    customerService.getOutstandingDebt(c.getAccountId())
            });
        }
    }

    public void refresh(){
        loadCustomersIntoTable();
    }


    private void resetForm() {
        tbl.clearSelection();

        accountIdTxt.setText("");
        holderNameTxt.setText("");
        contactNameTxt.setText("");
        addressTxt.setText("");
        phoneTxt.setText("");
        creditLimitTxt.setText("");
        agreedDiscountTxt.setText("");
        statusDrop.setSelectedIndex(0);

        accountIdTxt.setEditable(true);
    }
}