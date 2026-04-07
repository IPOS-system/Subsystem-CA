

package gui;

import dao.ItemDAO;
import domain.Item;
import service.AppController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SalesPage extends JPanel {


    JComboBox<String> customerTypeCmb; // "Account holder" | "Occasional"
    JTextField       accountIdTxt;    // enabled only for account‑holder
    JTable            saleTable;       // line‑items of the current sale
    DefaultTableModel saleModel;       // model for the table
    JLabel            totalLbl;        // running total
    JButton           addItemBtn;
    JButton           removeItemBtn;
    JButton           checkoutBtn;

    //read catalog
    private final ItemDAO itemDao = new ItemDAO();
    private final AppController appController;

    public SalesPage(AppController appController) {
        this.appController = appController;

        setLayout(new BorderLayout());

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
        add(makeCentrePanel(), BorderLayout.CENTER);
    }

    private JPanel makeCentrePanel() {
        JPanel centre = new JPanel(new BorderLayout(10, 10));
        centre.setBorder(new EmptyBorder(10, 10, 10, 10));
        centre.setOpaque(false);

        //account holder vs occassional

        JPanel topForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerTypeCmb = new JComboBox<>(new String[]{"Account holder", "Occasional"});
        accountIdTxt = new JTextField(12);
        accountIdTxt.setEnabled(false); // disabled for occasional customers

        topForm.add(new JLabel("Customer type:"));
        topForm.add(customerTypeCmb);
        topForm.add(new JLabel("Account ID (if holder):"));
        topForm.add(accountIdTxt);
        centre.add(topForm, BorderLayout.NORTH);



        //table of sales
        saleModel = new DefaultTableModel(
                new Object[]{"Item ID", "Description", "Qty", "Unit price", "Line total"},
                0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        saleTable = new JTable(saleModel);
        saleTable.setFillsViewportHeight(true);
        centre.add(new JScrollPane(saleTable), BorderLayout.CENTER);

    //activity centre
        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        totalLbl = new JLabel("Total: £0.00");
        totalLbl.setFont(totalLbl.getFont().deriveFont(Font.BOLD, 14f));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addItemBtn    = new JButton("Add Item");
        removeItemBtn = new JButton("Remove Item");
        checkoutBtn   = new JButton("Checkout");
        btnPanel.add(addItemBtn);
        btnPanel.add(removeItemBtn);
        btnPanel.add(checkoutBtn);

        bottom.add(totalLbl, BorderLayout.WEST);
        bottom.add(btnPanel, BorderLayout.EAST);
        centre.add(bottom, BorderLayout.SOUTH);

        hookEvents();

        return centre;
    }


    private void hookEvents() {
        // Enable / disable the Account‑ID field depending on the customer type.
        customerTypeCmb.addActionListener(e -> {
            boolean isAccount = customerTypeCmb.getSelectedIndex() == 0; // 0 = Account holder
            accountIdTxt.setEnabled(isAccount);
            if (!isAccount) {
                accountIdTxt.setText("");
            }
        });

        addItemBtn.addActionListener(e -> onAddItem());
        removeItemBtn.addActionListener(e -> onRemoveItem());
        checkoutBtn.addActionListener(e -> onCheckout());
    }

    private void onAddItem() {
        List<Item> items = itemDao.findAll();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No items are defined in the catalogue.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

      String[] colNames = {"Item ID", "Description", "Pack type", "Unit",
                "Units/Pack", "Pack cost"};
        DefaultTableModel catalogueModel = new DefaultTableModel(colNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Item i : items) {
            catalogueModel.addRow(new Object[]{
                    i.getItemId(),
                    i.getDescription(),
                    i.getPackageType(),
                    i.getUnit(),
                    i.getUnitsInPack(),
                    "£" + i.getPackageCost()
            });
        }

        JTable catalogueTbl = new JTable(catalogueModel);
        catalogueTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(catalogueTbl);
        scroll.setPreferredSize(new Dimension(800, 300));

        //quantity
        JTextField qtyField = new JTextField("1", 5);
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        qtyPanel.add(new JLabel("Quantity:"));
        qtyPanel.add(qtyField);

        //dialog
        JPanel dialogPanel = new JPanel(new BorderLayout(5, 5));
        dialogPanel.add(scroll, BorderLayout.CENTER);
        dialogPanel.add(qtyPanel, BorderLayout.SOUTH);

        int answer = JOptionPane.showConfirmDialog(this,
                dialogPanel,
                "Select product to add",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (answer != JOptionPane.OK_OPTION) {
            return; // user cancelled
        }

        int selectedRow = catalogueTbl.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product from the list.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //parse
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be a positive integer.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        Item chosen = items.get(selectedRow);


        BigDecimal packCost   = chosen.getPackageCost();
        int        unitsInPack = chosen.getUnitsInPack();

        // unit price = packCost / unitsInPack  (4‑decimal scale, HALF_UP)
        BigDecimal unitPrice = packCost.divide(
                BigDecimal.valueOf(unitsInPack),
                4,
                RoundingMode.HALF_UP);
        /*
        //purchase price of specific no. of tablets
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
        */

        BigDecimal lineTotal = chosen.getPackageCost().multiply(BigDecimal.valueOf((qty)));

        //add row
        saleModel.addRow(new Object[]{
                chosen.getItemId(),
                chosen.getDescription(),
                qty,
                unitPrice,
                lineTotal
        });
        recalculateTotal();
    }

    private void onRemoveItem() {
        int sel = saleTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a line‑item to remove.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove the selected line‑item?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            saleModel.removeRow(sel);
            recalculateTotal();
        }
    }

    private void onCheckout() {

        if (saleModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Add at least one item before checking out.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }


        if (customerTypeCmb.getSelectedIndex() == 0) { // account holder
            String acctId = accountIdTxt.getText().trim();
            if (acctId.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Enter the account id.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // TODO: call checkCreditLimit(acctId, getCurrentTotal())
        }

        //payment handling, reciet, statement
        JOptionPane.showMessageDialog(this,
                "Checkout logic not implemented yet.", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }


    private void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < saleModel.getRowCount(); i++) {
            Object value = saleModel.getValueAt(i, 4); // line‑total column
            try {
                total = total.add(new BigDecimal(value.toString()));
            } catch (Exception ignored) {}
        }
        totalLbl.setText("Total: £" + total.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal getCurrentTotal() {
        String txt = totalLbl.getText().replaceAll("[^0-9.]", "");
        return new BigDecimal(txt);
    }

    //WIP functions
    private boolean checkCreditLimit(String accountId, BigDecimal purchaseAmount) { return true; }
    private boolean processPayment() { return true; }
    private void generateReceipt() {}
    private void generateStatement() {}

    //reset sale
    private void resetSale() {
        saleModel.setRowCount(0);
        totalLbl.setText("Total: £0.00");
        customerTypeCmb.setSelectedIndex(1); // default to occasional
        accountIdTxt.setText("");
        accountIdTxt.setEnabled(false);
    }

    //testing
    public static void show(AppController controller) {
        JFrame frame = new JFrame("IPOS‑CA – Sales");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new SalesPage(controller));
        frame.setSize(950, 650);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
