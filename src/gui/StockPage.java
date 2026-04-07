package gui;

import service.AppController;
import service.ItemService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StockPage extends JPanel {

    private final AppController appController;
    private final ItemService itemService;




    private JTable tbl;
    private DefaultTableModel model;

    private JTextField itemIdTxt;
    private JTextField descriptionTxt;
    private JTextField packageTypeTxt;
    private JTextField unitTxt;
    private JTextField unitsInPackTxt;
    private JTextField packageCostTxt;
    private JTextField quantityTxt;
    private JTextField stockLimitTxt;

    private JButton addBtn;
    private JButton updateQtyBtn;
    private JButton removeBtn;
    private JButton deliveryBtn;
    private JButton lowStockBtn;
    private JButton clearBtn;

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

        model = new DefaultTableModel(
                new Object[]{
                        "item id",
                        "description",
                        "package type",
                        "unit",
                        "units/pack",
                        "cost",
                        "qty in stock",
                        "stock limit"
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
        form.setBorder(BorderFactory.createTitledBorder("manage stock"));

        itemIdTxt = new JTextField();
        descriptionTxt = new JTextField();
        packageTypeTxt = new JTextField();
        unitTxt = new JTextField();
        unitsInPackTxt = new JTextField();
        packageCostTxt = new JTextField();
        quantityTxt = new JTextField();
        stockLimitTxt = new JTextField();

        addBtn = new JButton("add item");
        updateQtyBtn = new JButton("update quantity");
        removeBtn = new JButton("remove item");
        deliveryBtn = new JButton("record delivery");
        lowStockBtn = new JButton("low stock");
        clearBtn = new JButton("clear");

        form.add(new JLabel("item id:"));
        form.add(itemIdTxt);
        form.add(new JLabel("description:"));
        form.add(descriptionTxt);

        form.add(new JLabel("package type:"));
        form.add(packageTypeTxt);
        form.add(new JLabel("unit:"));
        form.add(unitTxt);

        form.add(new JLabel("units in pack:"));
        form.add(unitsInPackTxt);
        form.add(new JLabel("package cost:"));
        form.add(packageCostTxt);

        form.add(new JLabel("quantity:"));
        form.add(quantityTxt);
        form.add(new JLabel("stock limit:"));
        form.add(stockLimitTxt);

        form.add(addBtn);
        form.add(updateQtyBtn);
        form.add(removeBtn);
        form.add(clearBtn);

        form.add(deliveryBtn);
        form.add(lowStockBtn);
        form.add(new JLabel(""));
        form.add(new JLabel(""));

        p.add(sp, BorderLayout.CENTER);
        p.add(form, BorderLayout.SOUTH);

        return p;
    }
}