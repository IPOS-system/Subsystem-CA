package gui;

import domain.DiscountPlan;
import domain.DiscountTier;
import service.AppController;
import service.DiscountPlanService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DiscountPlansPage extends JPanel {
    private final AppController appController;
    private final DiscountPlanService discountPlanService;

    private JLabel customerIdLabel;
    private String currentCustomerId;

    private JComboBox<String> planTypeDrop;
    private CardLayout cardLayout;
    private JPanel editorPanel;

    private JTextField fixedRateField;

    private JTable tierTable;
    private DefaultTableModel tierTableModel;

    public DiscountPlansPage(AppController appController) {
        this.appController = appController;
        this.discountPlanService = new DiscountPlanService();

        setLayout(new BorderLayout());
        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(buildMainPanel(), BorderLayout.CENTER);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
    }

    private JPanel buildMainPanel() {
        JPanel main = new JPanel(new BorderLayout(10, 10));

        JPanel top = new JPanel(new GridLayout(2, 1));
        customerIdLabel = new JLabel("Customer: none");

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("Plan type:"));

        planTypeDrop = new JComboBox<>(new String[]{"No plan", "Fixed", "Variable"});
        planTypeDrop.addActionListener(e -> updateVisibleEditor());
        typePanel.add(planTypeDrop);

        top.add(customerIdLabel);
        top.add(typePanel);

        main.add(top, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        editorPanel = new JPanel(cardLayout);
        editorPanel.add(buildNoPlanPanel(), "NONE");
        editorPanel.add(buildFixedPanel(), "FIXED");
        editorPanel.add(buildVariablePanel(), "VARIABLE");

        main.add(editorPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> savePlan());
        bottom.add(saveBtn);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            appController.showPage("customers");
        });
        bottom.add(backBtn);

        main.add(bottom, BorderLayout.SOUTH);
        updateVisibleEditor();

        return main;
    }

    private JPanel buildNoPlanPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("No discount plan assigned."));
        return panel;
    }

    private JPanel buildFixedPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Fixed discount %:"));
        fixedRateField = new JTextField(10);
        panel.add(fixedRateField);
        return panel;
    }

    private JPanel buildVariablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        tierTableModel = new DefaultTableModel(
                new Object[]{"Min Amount", "Max Amount", "Discount %"}, 0
        );
        tierTable = new JTable(tierTableModel);

        panel.add(new JScrollPane(tierTable), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addTierBtn = new JButton("Add Tier");
        addTierBtn.addActionListener(e -> tierTableModel.addRow(new Object[]{"", "", ""}));

        JButton removeTierBtn = new JButton("Remove Tier");
        removeTierBtn.addActionListener(e -> {
            int row = tierTable.getSelectedRow();
            if (row >= 0) {
                tierTableModel.removeRow(row);
            }
        });

        controls.add(addTierBtn);
        controls.add(removeTierBtn);

        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private void updateVisibleEditor() {
        String selected = (String) planTypeDrop.getSelectedItem();

        if ("Fixed".equals(selected)) {
            cardLayout.show(editorPanel, "FIXED");
        } else if ("Variable".equals(selected)) {
            cardLayout.show(editorPanel, "VARIABLE");
        } else {
            cardLayout.show(editorPanel, "NONE");
        }
    }

    public void setCurrentCustomerId(String id) {
        this.currentCustomerId = id;
        customerIdLabel.setText("Customer: " + id);
        loadPlan();
    }

    public void reset() {
        currentCustomerId = null;
        customerIdLabel.setText("Customer: none");
        planTypeDrop.setSelectedItem("No plan");
        fixedRateField.setText("");
        tierTableModel.setRowCount(0);
    }

    private void loadPlan() {
        fixedRateField.setText("");
        tierTableModel.setRowCount(0);

        if (currentCustomerId == null) {
            return;
        }

        try {
            DiscountPlan plan = discountPlanService.getCustomerDiscountPlan(currentCustomerId);

            if (plan == null) {
                planTypeDrop.setSelectedItem("No plan");
            } else if (DiscountPlan.TYPE_FIXED.equals(plan.getPlanType())) {
                planTypeDrop.setSelectedItem("Fixed");
                if (plan.getFixedRate() != null) {
                    fixedRateField.setText(plan.getFixedRate().toPlainString());
                }
            } else if (DiscountPlan.TYPE_TIERED.equals(plan.getPlanType())) {
                planTypeDrop.setSelectedItem("Variable");

                for (DiscountTier tier : plan.getTiers()) {
                    tierTableModel.addRow(new Object[]{
                            tier.getMinAmount(),
                            tier.getMaxAmount(),
                            tier.getDiscountRate()
                    });
                }
            } else {
                planTypeDrop.setSelectedItem("No plan");
            }

            updateVisibleEditor();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePlan() {
        if (currentCustomerId == null) {
            JOptionPane.showMessageDialog(this, "No customer selected.");
            return;
        }

        try {
            String selected = (String) planTypeDrop.getSelectedItem();

            if ("No plan".equals(selected)) {
                discountPlanService.clearCustomerDiscountPlan(currentCustomerId);
            } else if ("Fixed".equals(selected)) {
                BigDecimal rate = new BigDecimal(fixedRateField.getText().trim());
                discountPlanService.saveFixedPlan(currentCustomerId, rate);
            } else if ("Variable".equals(selected)) {
                discountPlanService.saveTieredPlan(currentCustomerId, readTiersFromTable());
            }

            JOptionPane.showMessageDialog(this, "Discount plan saved.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<DiscountTier> readTiersFromTable() {
        List<DiscountTier> tiers = new ArrayList<>();

        for (int i = 0; i < tierTableModel.getRowCount(); i++) {
            String minText = valueAt(i, 0);
            String maxText = valueAt(i, 1);
            String rateText = valueAt(i, 2);

            if (minText.isBlank() || rateText.isBlank()) {
                throw new IllegalArgumentException("Tier row " + (i + 1) + " is incomplete.");
            }

            BigDecimal min = new BigDecimal(minText);
            BigDecimal max = maxText.isBlank() ? null : new BigDecimal(maxText);
            BigDecimal rate = new BigDecimal(rateText);

            tiers.add(new DiscountTier(min, max, rate));
        }

        return tiers;
    }

    private String valueAt(int row, int col) {
        Object value = tierTableModel.getValueAt(row, col);
        return value == null ? "" : value.toString().trim();
    }
}