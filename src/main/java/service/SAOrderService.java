package service;

import api_impl.IAccInfoAPIService;
import api_impl.SAService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Item;
import service.Result;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;


import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SAOrderService {
    private IAccInfoAPIService iAccInfoAPIService;
    private ItemService itemService;
    private final Map<String, List<Item>> orderItemsByOrder = new HashMap<>();
    public SAOrderService(SAService saService){
        this.iAccInfoAPIService = new IAccInfoAPIService(saService);
        this.itemService = new ItemService();
    }


    public Result login(JPanel parent ){
        while (true) {
            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            usernameField.setText("cosymed");
            passwordField.setText("cosymed_password");

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
                return Result.fail("cancelled... continuing without SA Connection");
            }

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isBlank() || password.isBlank()) {
                JOptionPane.showMessageDialog(parent, "Enter username and password.");
                continue;
            }

            Result connection = iAccInfoAPIService.connect(username, password);
            if (connection.isSuccess()) {
                //JOptionPane.showMessageDialog(parent, iAccInfoAPIService.getAccountStatus().getMessage());
                return Result.success("connected successfully to IPOS SA");
            }
            JOptionPane.showMessageDialog(parent, "Invalid username or password or IPOS SA is offline");
        }
    }

    public Result getAccountStatus() {
        return iAccInfoAPIService.getAccountStatus();
    }

    public Result isConnectedToSA(){
        if( iAccInfoAPIService.isConnected()){
            return Result.success("successfully connected\n to ipos SA. ");
        }
        return Result.fail("IPOS SA connection failed... check server status or credentials");
    }


    public void showOrderHistory(JPanel parent) {
        int merchId = iAccInfoAPIService.getMerchantId();
        if (merchId == -1) {
            JOptionPane.showMessageDialog(parent, "log into IPOS SA first....");
            return;
        }

        Result res = iAccInfoAPIService.getOrderHistory(merchId);
        if (!res.isSuccess()) {
            JOptionPane.showMessageDialog(parent, res.getMessage(), "Order History", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode orders = mapper.readTree(res.getMessage());

            orderItemsByOrder.clear();

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Order ID", "Status", "Total Amount"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (JsonNode order : orders) {
                String orderId = order.get("orderId").asText();

                java.util.List<Item> itemsForThisOrder = new ArrayList<>();
                for (JsonNode itemJson : order.get("items")) {
                    itemsForThisOrder.add(new Item(
                            itemJson.get("productId").asText().replaceAll("[\\s-]", ""),
                            itemJson.get("description").asText(),
                            "",
                            "",
                            0,
                            BigDecimal.ZERO,
                            itemJson.get("quantity").asInt(),
                            0,
                            0
                    ));
                }

                orderItemsByOrder.put(orderId, itemsForThisOrder);

                model.addRow(new Object[]{
                        orderId,
                        order.get("status").asText(),
                        order.get("totalAmount").asText()
                });
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            JButton deliveredBtn = new JButton("Mark as Delivered");

            JButton invoicesBtn = new JButton("Show all invoices");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(deliveredBtn);
            buttonPanel.add(invoicesBtn);


            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Order History", true);
            dialog.setLayout(new BorderLayout());
            dialog.add(scrollPane, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);


            //dialog.add(invoicesBtn);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(parent);

            invoicesBtn.addActionListener(e -> {
                showInvoices(parent );
            });

            deliveredBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Select an order first.");
                    return;
                }

                String orderId = model.getValueAt(row, 0).toString();
                String status = model.getValueAt(row, 1).toString();

                if ("DELIVERED".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(dialog, "Order already delivered.");
                    return;
                }

                Result updateRes = null;

                if ("ACCEPTED".equalsIgnoreCase(status)) {
                    updateRes = iAccInfoAPIService.updateOrderStatus(orderId, "PROCESSING");
                    if (!updateRes.isSuccess()) {
                        JOptionPane.showMessageDialog(dialog, updateRes.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    updateRes = iAccInfoAPIService.updateOrderStatus(orderId, "DISPATCHED");
                    if (!updateRes.isSuccess()) {
                        JOptionPane.showMessageDialog(dialog, updateRes.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    updateRes = iAccInfoAPIService.updateOrderStatus(orderId, "DELIVERED");
                } else if ("PROCESSING".equalsIgnoreCase(status)) {
                    updateRes = iAccInfoAPIService.updateOrderStatus(orderId, "DISPATCHED");
                    if (!updateRes.isSuccess()) {
                        JOptionPane.showMessageDialog(dialog, updateRes.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    updateRes = iAccInfoAPIService.updateOrderStatus(orderId, "DELIVERED");
                } else if ("DISPATCHED".equalsIgnoreCase(status)) {
                    updateRes = iAccInfoAPIService.updateOrderStatus(orderId, "DELIVERED");
                } else if ("DELIVERED".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(dialog, "Order already delivered.");
                    return;
                } else {
                    JOptionPane.showMessageDialog(dialog, "Unsupported status: " + status);
                    return;
                }

                if (!updateRes.isSuccess()) {
                    JOptionPane.showMessageDialog(dialog, updateRes.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                for (Item item : orderItemsByOrder.get(orderId)) {
                    itemService.addQtyToStock(item.getItemId(), item.getQtyInStock());
                }

                model.setValueAt("DELIVERED", row, 1);
                JOptionPane.showMessageDialog(dialog, "Order marked as delivered.");
            });

            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, e.toString(), "Order History", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showInvoices(JPanel parent) {
        int merchId = iAccInfoAPIService.getMerchantId();
        if (merchId == -1) {
            JOptionPane.showMessageDialog(parent, "log into IPOS SA first....");
            return;
        }

        Result res = iAccInfoAPIService.listInvoices(merchId);

        if (!res.isSuccess()) {
            JOptionPane.showMessageDialog(parent, res.getMessage(), "Invoices", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode invoices = mapper.readTree(res.getMessage());

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Invoice ID", "Amount Due", "Payment Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (JsonNode invoice : invoices) {
                model.addRow(new Object[]{
                        invoice.path("invoiceId").asText(),
                        invoice.path("amountDue").asText(),
                        invoice.path("paymentStatus").asText()
                });
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600, 300));

            JButton generateInvoiceBtn = new JButton("Generate Invoice");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(generateInvoiceBtn);



            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Invoices", true);
            dialog.setLayout(new BorderLayout());
            dialog.add(scrollPane, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(parent);

            generateInvoiceBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Select an invoice first.");
                    return;
                }

                String invoiceId = model.getValueAt(row, 0).toString();
                showInvoiceDetails(parent, invoiceId);
            });

            dialog.setVisible(true);



        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, e.toString(), "Invoices", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Item orderLineToItem(JsonNode itemJson) {
        String itemId = itemJson.get("productId").asText().replaceAll("[\\s-]", "");
        String description = itemJson.get("description").asText();
        int quantity = itemJson.get("quantity").asInt();

        return new Item(
                itemId,
                description,
                "",                  // packageType not needed
                "",                  // unit not needed
                0,                   // unitsInPack not needed
                BigDecimal.ZERO,     // packageCost not needed
                quantity,            // use qtyInStock to carry delivered qty
                0,
                0
        );
    }

    public void showInvoiceDetails(JPanel parent, String invoiceId) {
        Result res = iAccInfoAPIService.getInvoice(invoiceId);

        if (!res.isSuccess()) {
            JOptionPane.showMessageDialog(parent, res.getMessage(), "Invoice Details", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(res.getMessage());

            String orderId = json.path("orderId").asText();

            StringBuilder sb = new StringBuilder();
            sb.append("Invoice ID -> ").append(json.path("invoiceId").asText()).append("\n");
            sb.append("Order ID -> ").append(orderId).append("\n");
            sb.append("Merchant Name -> ").append(json.path("merchantName").asText()).append("\n");
            sb.append("Invoice Date -> ").append(json.path("invoiceDate").asText()).append("\n");
            sb.append("Due Date -> ").append(json.path("dueDate").asText()).append("\n");
            sb.append("Amount Due -> ").append(json.path("amountDue").asText()).append("\n");
            sb.append("Total Paid -> ").append(json.path("totalPaid").asText()).append("\n");
            sb.append("Payment Status -> ").append(json.path("paymentStatus").asText()).append("\n");

            sb.append("\nOrder Items:\n");

            List<Item> items = orderItemsByOrder.get(orderId);
            if (items == null || items.isEmpty()) {
                sb.append("No order items loaded.\n");
            } else {
                for (Item item : items) {
                    sb.append("- ")
                            .append(item.getItemId())
                            .append(" | ")
                            .append(item.getDescription())
                            .append(" | qty: ")
                            .append(item.getQtyInStock())
                            .append("\n");
                }
            }

            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setPreferredSize(new Dimension(500, 350));

            JOptionPane.showMessageDialog(parent, scrollPane, "Invoice Details", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, e.toString(), "Invoice Details", JOptionPane.ERROR_MESSAGE);
        }
    }

    public java.util.List<Item> getItemsForOrder(String orderId) {
        return orderItemsByOrder.get(orderId);
    }



}
