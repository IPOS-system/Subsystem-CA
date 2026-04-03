package gui;

import dao.CustomerAccountDAO;
import domain.CustomerAccount;
import service.AppController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.RecursiveTask;

//right now the customers page does way too much
//it should be refactored into customerspage and customersservice
//that is a big job though


public class CustomersPage extends JPanel {

    private final AppController appController;
    private final CustomerAccountDAO customerDao;

    private JTable tbl;
    private DefaultTableModel model;

    private JTextField accountIdTxt;
    private JTextField holderNameTxt;
    private JTextField contactNameTxt;
    private JTextField addressTxt;
    private JTextField phoneTxt;
    private JTextField creditLimitTxt;
    private JTextField agreedDiscountTxt;   // shows fixed / tiered
    private JComboBox<String> statusDrop;

    private JButton addBtn;
    private JButton delBtn;
    private JButton clrBtn;

    private JButton updBtn;
    private JButton discountBtn;

    public CustomersPage(AppController appController) {
        this.appController = appController;
        this.customerDao = new CustomerAccountDAO();

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
                        "account id",
                        "holder name",
                        "contact",
                        "phone",
                        "credit limit",
                        "discount plan",
                        "status"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tbl = new JTable(model);
        JScrollPane sp = new JScrollPane(tbl);

        JPanel form = new JPanel(new GridLayout(6, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("manage customer accounts"));

        accountIdTxt = new JTextField();
        holderNameTxt = new JTextField();
        contactNameTxt = new JTextField();
        addressTxt = new JTextField();
        phoneTxt = new JTextField();
        creditLimitTxt = new JTextField();

        agreedDiscountTxt = new JTextField();
        agreedDiscountTxt.setEditable(false); // display only

        statusDrop = new JComboBox<>(new String[]{
                "active",
                "inactive"
        });

        addBtn = new JButton("add account");
        delBtn = new JButton("delete account");
        clrBtn = new JButton("clear");

        updBtn = new JButton("update details");
        discountBtn = new JButton("view/edit discount plans");

        form.add(new JLabel("account id:"));
        form.add(accountIdTxt);
        form.add(new JLabel("holder name:"));
        form.add(holderNameTxt);

        form.add(new JLabel("contact name:"));
        form.add(contactNameTxt);
        form.add(new JLabel("address:"));
        form.add(addressTxt);

        form.add(new JLabel("phone:"));
        form.add(phoneTxt);
        form.add(new JLabel("credit limit:"));
        form.add(creditLimitTxt);

        form.add(new JLabel("agreed discount:"));
        form.add(agreedDiscountTxt);
        form.add(new JLabel("status:"));
        form.add(statusDrop);

        form.add(addBtn);
        form.add(delBtn);
        form.add(updBtn);
        form.add(clrBtn);

        form.add(discountBtn);
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

                CustomerAccount c = customerDao.findById(accountId);

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

        addBtn.addActionListener(e -> {
            String accountId = accountIdTxt.getText().trim();
            String holderName = holderNameTxt.getText().trim();
            String contactName = contactNameTxt.getText().trim();
            String address = addressTxt.getText().trim();
            String phone = phoneTxt.getText().trim();
            String creditText = creditLimitTxt.getText().trim();
            String status = statusDrop.getSelectedItem().toString();

            if (accountId.isEmpty() || holderName.isEmpty() || creditText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "account id, holder name and credit limit required.");
                return;
            }

            BigDecimal creditLimit;

            try {
                creditLimit = new BigDecimal(creditText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "credit limit must be a valid number.");
                return;
            }

            CustomerAccount c = new CustomerAccount();
            c.setAccountId(accountId);
            c.setAccountHolderName(holderName);
            c.setContactName(contactName);
            c.setAddress(address);
            c.setPhone(phone);
            c.setCreditLimit(creditLimit);
            c.setAgreedDiscountId(null); // no selector wired yet
            c.setAccountStatus(status);

            boolean ok = customerDao.createCustomerAccount(c);

            if (ok) {
                JOptionPane.showMessageDialog(this, "customer account created.");
                resetForm();
                loadCustomersIntoTable();
            } else {
                JOptionPane.showMessageDialog(this, "could not create customer account.");
            }
        });

        delBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "select account first.");
                return;
            }

            String accountId = accountIdTxt.getText().trim();

            int yesNo = JOptionPane.showConfirmDialog(
                    this,
                    "delete account: " + accountId + "?",
                    "confirm delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (yesNo == JOptionPane.YES_OPTION) {
                boolean ok = customerDao.deleteCustomerAccount(accountId);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "customer account deleted.");
                    resetForm();
                    loadCustomersIntoTable();
                } else {
                    JOptionPane.showMessageDialog(this, "could not delete customer account.");
                }
            }
        });

        clrBtn.addActionListener(e -> resetForm());

        updBtn.addActionListener(e -> {
            String accountId = accountIdTxt.getText().trim();
            String holderName = holderNameTxt.getText().trim();
            String contactName = contactNameTxt.getText().trim();
            String address = addressTxt.getText().trim();
            String phone = phoneTxt.getText().trim();
            String creditText = creditLimitTxt.getText().trim();
            String status = statusDrop.getSelectedItem().toString();

            if (accountId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "select account first.");
                return;
            }

            if (holderName.isEmpty() || creditText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "holder name and credit limit required.");
                return;
            }

            BigDecimal creditLimit;

            try {
                creditLimit = new BigDecimal(creditText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "credit limit must be a valid number.");
                return;
            }

            CustomerAccount c = new CustomerAccount();
            c.setAccountId(accountId);
            c.setAccountHolderName(holderName);
            c.setContactName(contactName);
            c.setAddress(address);
            c.setPhone(phone);
            c.setCreditLimit(creditLimit);
            c.setAccountStatus(status);

            boolean ok = customerDao.updateCustomerAccount(c);

            if (ok) {
                JOptionPane.showMessageDialog(this, "customer account updated.");
                resetForm();
                loadCustomersIntoTable();
            } else {
                JOptionPane.showMessageDialog(this, "could not update customer account.");
            }
        });

        discountBtn.addActionListener(e ->{
            String accountId = accountIdTxt.getText();
            if(accountId.isEmpty()){
                JOptionPane.showMessageDialog(this, "select account first.");
                return;
            }
            appController.showDiscountPlanPage(accountId);
        }
        );
    }

    private void loadCustomersIntoTable() {
        model.setRowCount(0);

        List<CustomerAccount> allCustomers = customerDao.getAllCustomerAccounts();

        for (CustomerAccount c : allCustomers) {
            model.addRow(new Object[]{
                    c.getAccountId(),
                    c.getAccountHolderName(),
                    c.getContactName(),
                    c.getPhone(),
                    c.getCreditLimit(),
                    c.getDiscountPlanType(),
                    c.getAccountStatus()
            });
        }
    }

    public void refreshTable(){
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