package gui;

import service.AppController;
import service.TemplateService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TemplatesPage extends JPanel {

    private static final String TEMPLATE_NAME = "receipt";

    private static final String DEFAULT_RECEIPT_TEMPLATE =
            "{SHOP_NAME}\n" +
                    "{ADDRESS}\n" +
                    "Tel: {PHONE}\n\n" +
                    "--------------------------------\n" +
                    "{ITEMS}\n" +
                    "--------------------------------\n" +
                    "Total: {TOTAL}\n\n" +
                    "--------------------------------\n\n" +
                    "{FOOTER}";

    private final TemplateService templateService = new TemplateService();

    private JTextArea preview;
    private JTextField nameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField footerField;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public TemplatesPage(AppController appController) {
        setLayout(new BorderLayout());

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(buildTopNav(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(buildReceiptPage(), "receipts");

        center.add(contentPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        initialiseTemplate();
        loadSavedValuesIntoFields();
        updatePreview();
    }

    private JPanel buildTopNav() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton receiptBtn = new JButton("Receipts");
        receiptBtn.addActionListener(e -> cardLayout.show(contentPanel, "receipts"));

        panel.add(receiptBtn);
        return panel;
    }

    private JPanel buildReceiptPage() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        preview = new JTextArea();
        preview.setFont(new Font("Monospaced", Font.PLAIN, 14));
        preview.setEditable(false);

        JScrollPane previewScroll = new JScrollPane(preview);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(300, 0));

        nameField = new JTextField(15);
        addressField = new JTextField(15);
        phoneField = new JTextField(15);
        footerField = new JTextField(15);

        form.add(buildField("Shop Name", nameField));
        form.add(buildField("Address", addressField));
        form.add(buildField("Phone", phoneField));
        form.add(buildField("Footer", footerField));

        form.add(Box.createVerticalStrut(15));

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveTemplateValues());

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> resetToDefaults());

        form.add(saveBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(resetBtn);

        nameField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updatePreview());
        addressField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updatePreview());
        phoneField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updatePreview());
        footerField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updatePreview());

        panel.add(previewScroll, BorderLayout.CENTER);
        panel.add(form, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JLabel lbl = new JLabel(label);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setPreferredSize(new Dimension(250, 30));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private void initialiseTemplate() {
        try {
            templateService.ensureTemplateExists(TEMPLATE_NAME, DEFAULT_RECEIPT_TEMPLATE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to initialise template");
        }
    }

    private void loadSavedValuesIntoFields() {
        Map<String, String> values = getDefaultReceiptValues();

        try {
            values.putAll(templateService.loadValues(TEMPLATE_NAME));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load saved values");
        }

        nameField.setText(values.get("SHOP_NAME"));
        addressField.setText(values.get("ADDRESS"));
        phoneField.setText(values.get("PHONE"));
        footerField.setText(values.get("FOOTER"));
    }

    private void updatePreview() {
        try {
            String template = templateService.loadTemplate(TEMPLATE_NAME);
            template = templateService.applyValues(template, getCurrentFieldValues());
            template = template.replace("{ITEMS}", "Item A   £1.00\nItem B   £2.00");
            template = template.replace("{TOTAL}", "£3.00");
            preview.setText(template);
        } catch (IOException e) {
            preview.setText("Failed to load template");
        }
    }

    private void saveTemplateValues() {
        try {
            templateService.saveValues(TEMPLATE_NAME, getCurrentFieldValues());
            updatePreview();
            JOptionPane.showMessageDialog(this, "Saved");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Save failed");
        }
    }

    private void resetToDefaults() {
        try {
            templateService.resetValues(TEMPLATE_NAME);

            Map<String, String> defaults = getDefaultReceiptValues();
            nameField.setText(defaults.get("SHOP_NAME"));
            addressField.setText(defaults.get("ADDRESS"));
            phoneField.setText(defaults.get("PHONE"));
            footerField.setText(defaults.get("FOOTER"));

            updatePreview();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Reset failed");
        }
    }

    private Map<String, String> getCurrentFieldValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("SHOP_NAME", nameField.getText().trim());
        values.put("ADDRESS", addressField.getText().trim());
        values.put("PHONE", nameField.getText().isBlank() ? phoneField.getText().trim() : phoneField.getText().trim());
        values.put("FOOTER", footerField.getText().trim());
        return values;
    }

    private Map<String, String> getDefaultReceiptValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("SHOP_NAME", "Cosymed LTD.");
        values.put("ADDRESS", "123 High Street");
        values.put("PHONE", "01234 567890");
        values.put("FOOTER", "Thanks for your business!");
        return values;
    }

    @FunctionalInterface
    interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }
    }
}