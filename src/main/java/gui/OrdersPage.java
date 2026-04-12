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

    private final CatalogueService catalogueService;
    private final SaleService orderService;
    private final AppController appController;

    public OrdersPage(AppController appController, SaleService orderService, CatalogueService catalogueService) {
        this.appController = appController;
        this.orderService = orderService;
        this.catalogueService = catalogueService;

        setLayout(new BorderLayout());

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
        add(makeCentrePanel(), BorderLayout.CENTER);
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

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

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

    private void hookEvents() {
        addItemBtn.addActionListener(e -> onAddItem());
        removeItemBtn.addActionListener(e -> onRemoveItem());
        placeOrderBtn.addActionListener(e -> onPlaceOrder());
        clearBtn.addActionListener(e -> onClearBtn());
    }

    private void updateBasketTable() {
        basketModel.setRowCount(0);

        for (SaleItem item : orderService.getBasket()) {
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

            Result res = orderService.addItemToBasket(item, qty);
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

        orderService.removeItemFromBasket(basketTable.getValueAt(row, 0).toString());
        updateBasketTable();
        recalculateTotal();
    }

    private void onPlaceOrder() {
        if (basketModel.getRowCount() == 0) return;

        Result res = orderService.placeOrder();
        JOptionPane.showMessageDialog(this, res.getMessage());

        if (res.isSuccess()) {
            updateBasketTable();
            recalculateTotal();
        }
    }

    private void onClearBtn(){
        orderService.clearBasket();
        updateBasketTable();
        recalculateTotal();
    }

    private void recalculateTotal() {
        BigDecimal total = orderService.getTotal();
        totalLbl.setText("Total: £" + total.setScale(2, RoundingMode.HALF_UP));
    }
}