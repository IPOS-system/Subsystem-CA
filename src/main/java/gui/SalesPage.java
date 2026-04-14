

package gui;

import domain.*;
import service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SalesPage extends JPanel {


    //JComboBox<String> customerTypeCmb; //
    //JTextField       accountIdTxt;    //
    JTable basketTable;       //basket
    DefaultTableModel basketModel;       //model for the basket tabl;e
    JLabel            totalLbl;        //running total
    JLabel              currentCustLbl;
    JButton           addItemBtn;
    JButton           removeItemBtn;
    JButton           checkoutBtn;
    JButton clearSearchBtn;

    JTable catalogueTable;
    DefaultTableModel catalogueModel;

    JTextField searchField;
    JButton searchBtn;
    JButton clearBtn;


    private JComboBox<String> customerCmb;

    JButton noAccBtn;
    JButton selectCustomerBtn;


    //read catalog
    //private final ItemDAO itemDao = new ItemDAO();
    private final ItemService itemService;
    private final SaleService saleService;
    private final AppController appController;

    public SalesPage(AppController appController, SaleService saleService,
                     ItemService itemService) {
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
                new Object[]{"Item ID", "Description", "Qty", "Unit price", "total"},
                0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        basketTable = new JTable(basketModel);
        basketTable.setFillsViewportHeight(true);
        basketTable.getTableHeader().setReorderingAllowed(false);

        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         customerCmb = new JComboBox<>();
        selectCustomerBtn = new JButton("Select");

        noAccBtn = new JButton("no account");

        loadCustomers();

        customerPanel.add(new JLabel("Customer:"));
        customerPanel.add(customerCmb);
        customerPanel.add(selectCustomerBtn);
        customerPanel.add(noAccBtn);




        // search bar (top)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchBtn = new JButton("Search");
        clearSearchBtn = new JButton("Clear");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        searchPanel.add(searchBtn);
        searchPanel.add(clearSearchBtn); // <- add this

        // catalogue table (LEFT)
        catalogueModel = new DefaultTableModel(
                new Object[]{"Item ID", "Description", "Pack type", "Unit", "Units/Pack", "Pack cost", "QTY in stock"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };



        catalogueTable = new JTable(catalogueModel);
        catalogueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogueTable.getTableHeader().setReorderingAllowed(false);

        JPanel cataloguePanel = new JPanel(new BorderLayout(5, 5));

        // add components to cataloge panel

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        topPanel.add(customerPanel);
        topPanel.add(searchPanel);

        cataloguePanel.add(topPanel, BorderLayout.NORTH);
        cataloguePanel.add(new JScrollPane(catalogueTable), BorderLayout.CENTER);
        // load data once
        loadCatalogue();

        // split pane
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                cataloguePanel,                 //now includes search bar
                new JScrollPane(basketTable)
        );
        splitPane.setResizeWeight(0.5);
        splitPane.setEnabled(false);

        centre.add(splitPane, BorderLayout.CENTER);

        //activity centre
        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        totalLbl = new JLabel("Total: £0.00");
        totalLbl.setFont(totalLbl.getFont().deriveFont(Font.BOLD, 14f));

        currentCustLbl =  new JLabel();
        currentCustLbl.setFont(currentCustLbl.getFont().deriveFont(Font.BOLD, 14f));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addItemBtn    = new JButton("Add Item");
        removeItemBtn = new JButton("Remove Item");
        checkoutBtn   = new JButton("Checkout");
        clearBtn = new JButton("clear basket");

        btnPanel.add(addItemBtn);
        btnPanel.add(removeItemBtn);
        btnPanel.add(checkoutBtn);
        btnPanel.add(clearBtn);


        bottom.add(totalLbl, BorderLayout.WEST);
        bottom.add(currentCustLbl, BorderLayout.CENTER);
        bottom.add(btnPanel, BorderLayout.EAST);
        centre.add(bottom, BorderLayout.SOUTH);

        hookEvents();

        return centre;
    }

    //this should be renamed to load stock or something // its NOT the catalogue
    private void loadCatalogue() {
        catalogueModel.setRowCount(0); //reset
        List<Item> items = itemService.findAll();

        for (Item i : items) {
            catalogueModel.addRow(new Object[]{
                    i.getItemId(),
                    i.getDescription(),
                    i.getPackageType(),
                    i.getUnit(),
                    i.getUnitsInPack(),
                    i.getPackageCost(),
                    i.getQtyInStock()
            });
        }
    }

    private void filterCatalogue() {
        String text = searchField.getText().trim().toLowerCase();

        catalogueModel.setRowCount(0);

        List<Item> items = itemService.findAll();

        for (Item i : items) {
            if (text.isEmpty() ||
                    i.getItemId().toLowerCase().contains(text) ||
                    i.getDescription().toLowerCase().contains(text)) {

                catalogueModel.addRow(new Object[]{
                        i.getItemId(),
                        i.getDescription(),
                        i.getPackageType(),
                        i.getUnit(),
                        i.getUnitsInPack(),
                        i.getPackageCost(),
                        i.getQtyInStock()
                });
            }
        }
    }


    private void hookEvents() {

        addItemBtn.addActionListener(e -> onAddItem());
        removeItemBtn.addActionListener(e -> onRemoveItem());
        checkoutBtn.addActionListener(e -> onCheckout());
        clearBtn.addActionListener(e -> onClearBtn());
        selectCustomerBtn.addActionListener(e -> onSelectCustomer());
        noAccBtn.addActionListener(e -> onNoAccBtn());

        searchBtn.addActionListener(e -> filterCatalogue());
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            loadCatalogue();
        });

    }

    private PaymentInfo showPaymentDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        JRadioButton cashBtn = new JRadioButton("Cash");
        JRadioButton cardBtn = new JRadioButton("Card");
        JRadioButton accountBtn = new JRadioButton("Account");

        ButtonGroup group = new ButtonGroup();
        group.add(cashBtn);
        group.add(cardBtn);

        panel.add(cashBtn);
        panel.add(cardBtn);

        if (!(saleService.getCurrentCustomerName().equals( "no customer selected"))) {
            group.add(accountBtn);
            panel.add(accountBtn);
        }

        cashBtn.setSelected(true);

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Payment Method",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return null;

        PaymentInfo info = new PaymentInfo();

        if (cashBtn.isSelected()) {
            info.method= "cash";
        } else if (cardBtn.isSelected()) {
            String cardNumber = JOptionPane.showInputDialog(this, "Enter card number:");
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invalid card number");
                return null;
            }
            info.method = "card";
            info.cardNumber = cardNumber.trim();
        } else if (accountBtn.isSelected()) {
            info.method = "account";
        }
        return info;
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

    private void onNoAccBtn(){
        customerCmb.setSelectedIndex(-1);
        saleService.setCustomer(null);
        refresh();
    }


    private void onSelectCustomer(){
        String selected = (String) customerCmb.getSelectedItem();
        String accountId = (selected != null) ? selected.split(" - ")[0] : null;
        saleService.setCustomer(accountId);
        refresh();
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

        if(!addToBasketResult.isSuccess()) {
            JOptionPane.showMessageDialog(this, addToBasketResult.getMessage());
        }
        refresh();
    }


    private void onRemoveItem() {
        int sel = basketTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a item to remove.", "Info",
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
        PaymentInfo paymentMethod = showPaymentDialog(); //get payment meth
        //TODO null pointer exception

        if(paymentMethod == null){
            JOptionPane.showMessageDialog(this, "no payment method selected, try again");
            return;
        }

        Result saleResult = saleService.placeSale(paymentMethod);
        JOptionPane.showMessageDialog(this, saleResult.getMessage());

        //clear everything with refresh()
        recalculateTotal();
        updateBasketTable();
        recalculateTotal();
        loadCatalogue();

    }

    private void onClearBtn(){
        saleService.clearBasket();
        refresh();
    }

    private void loadCustomers(){
        customerCmb.removeAllItems();
        for (CustomerAccount c : saleService.getAllCustomers()) {

            customerCmb.addItem(c.getAccountId() + " - " + c.getAccountHolderName());
        }
    }



    private void recalculateTotal() {
        BigDecimal total = saleService.getTotal();
        totalLbl.setText("Total: £" + total.setScale(2, RoundingMode.HALF_UP));
    }

    private void showCurrentCustomer(){
        currentCustLbl.setText( "Current customer = "+ saleService.getCurrentCustomerName());
    };

    //reload the page to prevent stale data
    //we use items and customers which could change, so reload when this panel is switched to.
    public void refresh(){
        updateBasketTable();
        loadCatalogue();
        loadCustomers();
        recalculateTotal();
        showCurrentCustomer();
    }
}
