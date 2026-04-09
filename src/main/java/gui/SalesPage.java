

package gui;

import domain.Item;
import domain.SaleItem;
import service.AppController;
import service.ItemService;
import service.Result;
import service.SaleService;

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
    JTable basketTable;       // basket
    DefaultTableModel basketModel;       // model for the basket tabl;e
    JLabel            totalLbl;        // running total
    JButton           addItemBtn;
    JButton           removeItemBtn;
    JButton           checkoutBtn;

    JTable catalogueTable;
    DefaultTableModel catalogueModel;

    JTextField searchField;
    JButton searchBtn;

    //read catalog
    //private final ItemDAO itemDao = new ItemDAO();
    private final ItemService itemService;
    private final SaleService saleService;
    private final AppController appController;

    public SalesPage(AppController appController, SaleService saleService, ItemService itemService) {
        this.appController = appController;
        this.saleService = saleService;
        this.itemService = itemService;

        setLayout(new BorderLayout());

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
        add(makeCentrePanel(), BorderLayout.CENTER);
    }

    private JPanel makeCentrePanel() {
        JPanel centre = new JPanel(new BorderLayout(10, 10));
        centre.setBorder(new EmptyBorder(10, 10, 10, 10));
        centre.setOpaque(false);


        //basket table (RIGHT)
        basketModel = new DefaultTableModel(
                new Object[]{"Item ID", "Description", "Qty", "Unit price", "Line total"},
                0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        basketTable = new JTable(basketModel);
        basketTable.setFillsViewportHeight(true);
        basketTable.getTableHeader().setReorderingAllowed(false);



        // search bar (top)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchBtn = new JButton("Search");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // catalogue table (LEFT)
        catalogueModel = new DefaultTableModel(
                new Object[]{"Item ID", "Description", "Pack type", "Unit", "Units/Pack", "Pack cost"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };



        catalogueTable = new JTable(catalogueModel);
        catalogueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogueTable.getTableHeader().setReorderingAllowed(false);

        JPanel cataloguePanel = new JPanel(new BorderLayout(5, 5));

        // add components to cataloge panel
        cataloguePanel.add(searchPanel, BorderLayout.NORTH);
        cataloguePanel.add(new JScrollPane(catalogueTable), BorderLayout.CENTER);

        // load data once
        loadCatalogue();

        // split pane
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                cataloguePanel,                 // <-- now includes search bar
                new JScrollPane(basketTable)
        );
        splitPane.setResizeWeight(0.5);
        splitPane.setEnabled(false);

        centre.add(splitPane, BorderLayout.CENTER);

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

    private void loadCatalogue() {
        catalogueModel.setRowCount(0);
        List<Item> items = itemService.findAll();

        for (Item i : items) {
            catalogueModel.addRow(new Object[]{
                    i.getItemId(),
                    i.getDescription(),
                    i.getPackageType(),
                    i.getUnit(),
                    i.getUnitsInPack(),
                    i.getPackageCost()
            });
        }
    }


    private void hookEvents() {

        addItemBtn.addActionListener(e -> onAddItem());
        removeItemBtn.addActionListener(e -> onRemoveItem());
        checkoutBtn.addActionListener(e -> onCheckout());
    }

    //use this bad boy when you add/remove from basket
    private void updateBasketTable(){
        basketModel.setRowCount(0);//clear it

        for (SaleItem saleItem : saleService.getBasket()) {
            basketModel.addRow(new Object[]{
                    saleItem.getItemId(),
                    saleItem.getItemDescription(),
                    saleItem.getQuantity(),
                    saleItem.getUnitPrice(),
                    saleItem.getOrderItemPrice()
            });
        }
    }


    private void onAddItem() {

        int selectedRow = catalogueTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select an item from the catalogue.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (qtyStr == null) return;

        int qty;
        try {
            qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be a positive integer.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //can we get here with erroneous data?

        String itemId = catalogueTable.getValueAt(selectedRow, 0).toString();

        Item itemToAdd =  itemService.findById(itemId);//errors?


        // delegate logic to service
        Result addToBasketResult = saleService.addItemToBasket(itemToAdd,qty);
        updateBasketTable();

        if(!addToBasketResult.isSuccess()) {
            JOptionPane.showMessageDialog(this, addToBasketResult.getMessage());
        }
        recalculateTotal();

    }


    private void onRemoveItem() {
        int sel = basketTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a line‑item to remove.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove the selected item from basket?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            saleService.removeItemFromBasket(basketTable.getValueAt(sel, 0).toString());
            updateBasketTable();
            //basketModel.removeRow(sel);
            recalculateTotal();
        }
    }

    private void onCheckout() {

        if (basketModel.getRowCount() == 0) {
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
        BigDecimal total = saleService.getBasketTotal();
        totalLbl.setText("Total: £" + total.setScale(2, RoundingMode.HALF_UP));
    }



}
