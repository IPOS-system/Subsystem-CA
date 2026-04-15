package gui;

import domain.Item;
import domain.SaleItem;
import service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrdersPage extends JPanel {

    JTable basketTable;
    DefaultTableModel basketModel;
    JLabel totalLbl;
    JButton addItemBtn;
    JButton removeItemBtn;
    JButton placeOrderBtn;

    JTable catalogueTable;
    DefaultTableModel catalogueModel;

    JTextField searchField;
    JButton searchBtn;
    JButton clearBtn;

    JButton clearSearchBtn;

    private final CatalogueService catalogueService;
    private final SaleService saleService;
    private final AppController appController;

    public OrdersPage(AppController appController, SaleService saleService, CatalogueService catalogueService) {
        this.appController = appController;
        this.saleService = saleService;
        this.catalogueService = catalogueService;

        setLayout(new BorderLayout());

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
        add(makeCentrePanel(), BorderLayout.CENTER);




    }

    private Result promptForSALogin(Component parent) {
        while (true) {
            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.add(new JLabel("SA Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("SA Password:"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(
                    parent,
                    panel,
                    "Login to SA",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return Result.fail("chose nothing. ");
            }

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isBlank() || password.isBlank()) {
                JOptionPane.showMessageDialog(parent, "Enter username and password.");
                continue;
            }

            return saleService.connect(username, password);

        }
    }

    private JPanel makeCentrePanel() {
        JPanel centre = new JPanel(new BorderLayout(10, 10));
        centre.setBorder(new EmptyBorder(10, 10, 10, 10));
        centre.setOpaque(false);

        basketModel = new DefaultTableModel(
                new Object[]{"Item ID", "Description", "Qty", "Unit price", "Line total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        basketTable = new JTable(basketModel);
        basketTable.setFillsViewportHeight(true);
        basketTable.getTableHeader().setReorderingAllowed(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchBtn = new JButton("Search");
        clearSearchBtn = new JButton("Clear");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearSearchBtn);

        catalogueModel = new DefaultTableModel(
                new Object[]{"Item ID", "Description", "Pack type", "Unit", "Units/Pack", "Pack cost"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        catalogueTable = new JTable(catalogueModel);
        catalogueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogueTable.getTableHeader().setReorderingAllowed(false);

        JPanel cataloguePanel = new JPanel(new BorderLayout(5, 5));
        cataloguePanel.add(searchPanel, BorderLayout.NORTH);
        cataloguePanel.add(new JScrollPane(catalogueTable), BorderLayout.CENTER);

        loadCatalogue();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                cataloguePanel,
                new JScrollPane(basketTable)
        );
        splitPane.setResizeWeight(0.5);
        splitPane.setEnabled(false);

        centre.add(splitPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        totalLbl = new JLabel("Total: £0.00");
        totalLbl.setFont(totalLbl.getFont().deriveFont(Font.BOLD, 14f));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addItemBtn = new JButton("Add Item");
        removeItemBtn = new JButton("Remove Item");
        placeOrderBtn = new JButton("Place Order");
        clearBtn = new JButton("clear basket");


        btnPanel.add(addItemBtn);
        btnPanel.add(removeItemBtn);
        btnPanel.add(placeOrderBtn);
        btnPanel.add(clearBtn);

        bottom.add(totalLbl, BorderLayout.WEST);
        bottom.add(btnPanel, BorderLayout.EAST);
        centre.add(bottom, BorderLayout.SOUTH);

        hookEvents();

        return centre;
    }

    private void loadCatalogue() {
        catalogueModel.setRowCount(0);
        List<Item> items = catalogueService.findAll();

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
    private void filterCatalogue() {
        String text = searchField.getText().trim().toLowerCase();

        catalogueModel.setRowCount(0);

        List<Item> items = catalogueService.findAll();

        for (Item i : items) {
            if (text.isEmpty()
                    || i.getItemId().toLowerCase().contains(text)
                    || i.getDescription().toLowerCase().contains(text)) {

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
    }

    private void hookEvents() {
        addItemBtn.addActionListener(e -> onAddItem());
        removeItemBtn.addActionListener(e -> onRemoveItem());
        placeOrderBtn.addActionListener(e -> onPlaceOrder());
        clearBtn.addActionListener(e -> onClearBtn());

        searchBtn.addActionListener(e -> filterCatalogue());
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            loadCatalogue();
        });
        searchField.addActionListener(e -> filterCatalogue());
    }

    private void updateBasketTable() {
        basketModel.setRowCount(0);

        for (SaleItem item : saleService.getBasket()) {
            basketModel.addRow(new Object[]{
                    item.getItemId(),
                    item.getItemDescription(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getOrderItemPrice()
            });
        }
    }

    private void onAddItem() {
        int row = catalogueTable.getSelectedRow();
        if (row < 0) return;

        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (qtyStr == null) return;

        try {
            int qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0) throw new Exception();

            String itemId = catalogueTable.getValueAt(row, 0).toString();
            Item item = catalogueService.findById(itemId);

            Result res = saleService.addItemToBasket(item, qty);
            updateBasketTable();
            recalculateTotal();

            if (!res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage());
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "invalid quantity");
        }
    }

    private void onRemoveItem() {
        int row = basketTable.getSelectedRow();
        if (row < 0) return;

        saleService.removeItemFromBasket(basketTable.getValueAt(row, 0).toString());
        updateBasketTable();
        recalculateTotal();
    }

    private void onPlaceOrder() {
        if (basketModel.getRowCount() == 0) return;

        Result res = saleService.placeOrder();
        JOptionPane.showMessageDialog(this, res.getMessage());

        if (res.isSuccess()) {
            updateBasketTable();
            recalculateTotal();
        }
    }

    private void onClearBtn(){
        saleService.clearBasket();
        updateBasketTable();
        recalculateTotal();
    }

    private void recalculateTotal() {
        BigDecimal total = saleService.getTotal();
        totalLbl.setText("Total: £" + total.setScale(2, RoundingMode.HALF_UP));
    }

    public void refresh() {
        if(!saleService.isConnectedtoSA()){
            JOptionPane.showMessageDialog(this, promptForSALogin(this ).getMessage());
        }
    }
}