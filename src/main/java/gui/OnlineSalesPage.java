package gui;

import service.AppController;
import service.OnlineSaleService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class OnlineSalesPage extends JPanel {
    private final OnlineSaleService onlineSaleService;
    private final AppController appController;

    private JTable ordersTable;
    private DefaultTableModel ordersModel;
    private JTextArea itemsArea;
    private JButton refreshBtn;

    public OnlineSalesPage(AppController appController, OnlineSaleService onlineSaleService) {
        this.appController = appController;
        this.onlineSaleService = onlineSaleService;

        setLayout(new BorderLayout());

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
        add(makeCentrePanel(), BorderLayout.CENTER);
    }

    private JPanel makeCentrePanel() {
        JPanel centre = new JPanel(new BorderLayout(10, 10));
        centre.setBorder(new EmptyBorder(10, 10, 10, 10));
        centre.setOpaque(false);

        ordersModel = new DefaultTableModel(
                new Object[]{"Order ID", "Status", "Address", "Received At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(ordersModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getTableHeader().setReorderingAllowed(false);

        itemsArea = new JTextArea();
        itemsArea.setEditable(false);
        itemsArea.setLineWrap(true);
        itemsArea.setWrapStyleWord(true);

        refreshBtn = new JButton("Refresh");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshBtn);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(ordersTable),
                new JScrollPane(itemsArea)
        );

        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(200);
        splitPane.setEnabled(false);

        centre.add(topPanel, BorderLayout.NORTH);
        centre.add(splitPane, BorderLayout.CENTER);

        hookEvents();
        return centre;
    }

    private void hookEvents() {
        refreshBtn.addActionListener(e -> refresh());

        ordersTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedOrderItems();
            }
        });
    }

    public void refresh() {
        ordersModel.setRowCount(0);
        itemsArea.setText("");

        List<Map<String, Object>> orders = onlineSaleService.getAllOrders();

        for (Map<String, Object> order : orders) {
            ordersModel.addRow(new Object[]{
                    order.get("orderId"),
                    order.get("status"),
                    order.get("deliveryAddress"),
                    order.get("receivedAt")
            });
        }
    }

    private void showSelectedOrderItems() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) return;

        String orderId = ordersModel.getValueAt(row, 0).toString();
        List<Map<String, Object>> items = onlineSaleService.getItemsForOrder(orderId);

        StringBuilder sb = new StringBuilder();
        sb.append("Order: ").append(orderId).append("\n\n");

        for (Map<String, Object> item : items) {
            sb.append("Product ID: ").append(item.get("productId")).append("\n");
            sb.append("Quantity: ").append(item.get("quantity")).append("\n\n");
        }

        itemsArea.setText(sb.toString());
    }
}