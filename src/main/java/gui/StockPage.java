package gui;

import domain.Item;
import service.AppController;
import service.ItemService;
import service.Result;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class StockPage extends JPanel {

    private final AppController appController;
    private final ItemService itemService;

    private JTable tbl;
    private DefaultTableModel stockModel;

    private JTextField itemIdTxt;
    private JTextField descriptionTxt;
    private JTextField packageTypeTxt;
    private JTextField unitTxt;
    private JTextField unitsInPackTxt;
    private JTextField packageCostTxt;
    private JTextField quantityTxt;
    private JTextField stockLimitTxt;
    private JTextField markupTxt;
    private JTextField searchTxt;

    private JButton addBtn;
    private JButton updateQtyBtn;
    private JButton removeBtn;
    private JButton clearBtn;
    private JButton searchBtn;
    private JButton resetSearchBtn;

    public StockPage(AppController appController, ItemService itemService) {
        this.appController = appController;
        this.itemService = itemService;

        setLayout(new BorderLayout());

        add(new HeaderPanel(this.appController), BorderLayout.NORTH);
        add(makeMiddle(), BorderLayout.CENTER);
        add(new BottomPanel(this.appController), BorderLayout.SOUTH);
    }

    private JPanel makeMiddle() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setOpaque(false);

        // --- search bar ---
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchTxt = new JTextField();
        searchBtn = new JButton("Search");
        resetSearchBtn = new JButton("Reset Search");

        searchPanel.add(new JLabel("Search by quantity in stock:"), BorderLayout.WEST);
        searchPanel.add(searchTxt, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(searchBtn);
        btnPanel.add(resetSearchBtn);

        searchPanel.add(btnPanel, BorderLayout.EAST);
//        searchPanel.add(searchBtn, BorderLayout.EAST);
//        searchPanel.add(resetSearchBtn, BorderLayout.EAST);

        stockModel = new DefaultTableModel(
                new Object[]{
                        "Item ID",
                        "Description",
                        "Package Type",
                        "Unit",
                        "Units/Pack",
                        "Cost",
                        "Qty in Stock",
                        "Stock Limit",
                        "Markup"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tbl = new JTable(stockModel);
        tbl.getTableHeader().setReorderingAllowed(false);

        JScrollPane sp = new JScrollPane(tbl);

        JPanel form = new JPanel(new GridLayout(7, 4, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Manage Stock"));

        itemIdTxt = new JTextField();
        descriptionTxt = new JTextField();
        packageTypeTxt = new JTextField();
        unitTxt = new JTextField();
        unitsInPackTxt = new JTextField();
        packageCostTxt = new JTextField();
        quantityTxt = new JTextField();
        stockLimitTxt = new JTextField();
        markupTxt =  new JTextField();

        addBtn = new JButton("Add Item");
        updateQtyBtn = new JButton("Update Quantity");
        removeBtn = new JButton("Remove Item");
        clearBtn = new JButton("Deselect Item");

        form.add(new JLabel("Item ID:"));
        form.add(itemIdTxt);
        //itemIdTxt.setEditable(false);

        form.add(new JLabel("Description:"));
        form.add(descriptionTxt);
        //descriptionTxt.setEditable(false);

        form.add(new JLabel("Package Type:"));
        form.add(packageTypeTxt);

        form.add(new JLabel("Units:"));
        form.add(unitTxt);

        form.add(new JLabel("Units in Pack:"));
        form.add(unitsInPackTxt);
        form.add(new JLabel("Package Cost:"));
        form.add(packageCostTxt);

        form.add(new JLabel("Quantity:"));
        form.add(quantityTxt);

        form.add(new JLabel("Stock Limit:"));
        form.add(stockLimitTxt);

        form.add(new JLabel("Markup"));
        form.add(markupTxt);

        form.add(addBtn);
        form.add(updateQtyBtn);
        form.add(removeBtn);
        form.add(clearBtn);

        form.add(new JLabel(""));
        form.add(new JLabel(""));

        p.add(searchPanel, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        p.add(form, BorderLayout.SOUTH);

        updateTable();
        bindTableSelection();
        hookEvents();

        return p;
    }

    private void hookEvents(){
        addBtn.addActionListener(e -> onAddBtn());
        updateQtyBtn.addActionListener(e -> onModifyQtyBtn());
        removeBtn.addActionListener(e -> onDeleteBtn());
        clearBtn.addActionListener(e -> onClearBtn());
        searchBtn.addActionListener(e -> onSearchBtn());
        resetSearchBtn.addActionListener(e -> onResetSearchBtn());

    }

    private void onAddBtn(){
        try {
            String id = itemIdTxt.getText().trim();
            String desc = descriptionTxt.getText().trim();
            String pkgType = packageTypeTxt.getText().trim();
            String unit = unitTxt.getText().trim();

            int unitsInPack = Integer.parseInt(unitsInPackTxt.getText().trim());
            BigDecimal cost = new BigDecimal(packageCostTxt.getText().trim());
            int qty = Integer.parseInt(quantityTxt.getText().trim());
            int stockLimit = Integer.parseInt(stockLimitTxt.getText().trim());
            int markup = Integer.parseInt(markupTxt.getText().trim()); // or add a textbox if needed

            Item item = new Item(id, desc, pkgType, unit, unitsInPack, cost, qty, stockLimit, markup);

            Result res = itemService.addItemToStock(item);
            JOptionPane.showMessageDialog(this, res.getMessage());
            if(res.isSuccess()){
                updateTable();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    };

    private void onModifyQtyBtn() {
        try {
            int row = tbl.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a row first.");
                return;
            }

            String itemId = itemIdTxt.getText().trim();
            int newQty = Integer.parseInt(quantityTxt.getText().trim());

            if (newQty < 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.");
                return;
            }

            Result res = itemService.modifyQtyInStock(itemId, newQty);
            JOptionPane.showMessageDialog(this, res.getMessage());

            if (res.isSuccess()) {
                updateTable();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    private void onDeleteBtn() {
        try {
            int row = tbl.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a row first.");
                return;
            }

            String itemId = stockModel.getValueAt(row, 0).toString();

            Result res = itemService.removeItemFromStock(itemId);
            JOptionPane.showMessageDialog(this, res.getMessage());

            if (res.isSuccess()) {
                updateTable();
                onClearBtn();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting item.");
        }
    }

    private void onClearBtn(){
        itemIdTxt.setText("");
        descriptionTxt.setText("");
        packageTypeTxt.setText("");
        unitTxt.setText("");
        unitsInPackTxt.setText("");
        packageCostTxt.setText("");
        quantityTxt.setText("");
        stockLimitTxt.setText("");
        markupTxt.setText("");
    }

    private void onSearchBtn() {
        try {
            int threshold = Integer.parseInt(searchTxt.getText().trim());

            stockModel.setRowCount(0);

            for (Item i : itemService.findAll()) {
                if (i.getQtyInStock() < threshold) {
                    stockModel.addRow(new Object[]{
                            i.getItemId(),
                            i.getDescription(),
                            i.getPackageType(),
                            i.getUnit(),
                            i.getUnitsInPack(),
                            i.getPackageCost(),
                            i.getQtyInStock(),
                            i.getStockLimit(),
                            i.getMarkup()
                    });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid threshold.");
        }
    }

    private void onResetSearchBtn(){
        searchTxt.setText("");
        updateTable();
    }

    private void bindTableSelection() {
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tbl.getSelectedRow();
                if (row >= 0) {
                    itemIdTxt.setText(stockModel.getValueAt(row, 0).toString());
                    descriptionTxt.setText(stockModel.getValueAt(row, 1).toString());
                    packageTypeTxt.setText(stockModel.getValueAt(row, 2).toString());
                    unitTxt.setText(stockModel.getValueAt(row, 3).toString());
                    unitsInPackTxt.setText(stockModel.getValueAt(row, 4).toString());
                    packageCostTxt.setText(stockModel.getValueAt(row, 5).toString());
                    quantityTxt.setText(stockModel.getValueAt(row, 6).toString());
                    stockLimitTxt.setText(stockModel.getValueAt(row, 7).toString());
                    markupTxt.setText(stockModel.getValueAt(row, 8).toString());
                }
            }
        });
    }

    //reset aswell
    private void updateTable(){
        stockModel.setRowCount(0);
        for(Item i : itemService.findAll()){
            stockModel.addRow(new Object[]{
                    i.getItemId(),
                    i.getDescription(),
                    i.getPackageType(),
                    i.getUnit(),
                    i.getUnitsInPack(),
                    i.getPackageCost(),
                    i.getQtyInStock(),
                    i.getStockLimit(),
                    i.getMarkup()
            });
        }

    }
    public void refresh(){
        updateTable();
    }

}