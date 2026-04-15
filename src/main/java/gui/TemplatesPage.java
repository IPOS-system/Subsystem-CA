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

    private static final String REMINDER_TEMPLATE_NAME = "reminder1";

    private static final String DEFAULT_REMINDER_TEMPLATE =
            "Payment Overdue Reminder\n" +
                    "========================\n" +
                    "Customer account No. {CUSTACC}\n" +
                    "total amount {UNPAID}\n\n" +
                    "Dear {CLIENT},\n" +
                    "{OPENING MESSAGE}\n\n" +
                    "{BODY}\n\n" +
                    "{END MESSAGE}\n\n" +
                    "{SIGNED}";

    private final TemplateService templateService = new TemplateService();

    private JTextArea preview;
    private JTextField nameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField footerField;

    private JTextArea reminderPreview;
    private JTextField openingMessageField;
    private JTextArea bodyField;
    private JTextField endMessageField;
    private JTextField signedField;

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

        contentPanel.add(buildReminderPage(), "reminders");

        center.add(contentPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        initialiseTemplate();
        loadSavedValuesIntoFields();

        loadSavedReminderValuesIntoFields();
        updateReminderPreview();

        updatePreview();
    }

    private JPanel buildTopNav() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton receiptBtn = new JButton("Receipts");
        receiptBtn.addActionListener(e -> cardLayout.show(contentPanel, "receipts"));

        panel.add(receiptBtn);

        JButton reminderBtn = new JButton("Overdue Reminders");
        reminderBtn.addActionListener(e -> cardLayout.show(contentPanel, "reminders"));
        panel.add(reminderBtn);

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

    private JPanel buildReminderPage() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        reminderPreview = new JTextArea();
        reminderPreview.setFont(new Font("Monospaced", Font.PLAIN, 14));
        reminderPreview.setEditable(false);

        JScrollPane previewScroll = new JScrollPane(reminderPreview);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(320, 0));

        openingMessageField = new JTextField(15);
        bodyField = new JTextArea(8, 15);
        bodyField.setLineWrap(true);
        bodyField.setWrapStyleWord(true);
        endMessageField = new JTextField(15);
        signedField = new JTextField(15);

        form.add(buildField("Opening Message", openingMessageField));
        form.add(Box.createVerticalStrut(8));
        form.add(buildTextAreaField("Body", bodyField));
        form.add(Box.createVerticalStrut(8));
        form.add(buildField("End Message", endMessageField));
        form.add(Box.createVerticalStrut(8));
        form.add(buildField("Signed", signedField));

        form.add(Box.createVerticalStrut(15));

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveReminderValues());

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> resetReminderToDefaults());

        form.add(saveBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(resetBtn);

        openingMessageField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updateReminderPreview());
        bodyField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updateReminderPreview());
        endMessageField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updateReminderPreview());
        signedField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updateReminderPreview());

        panel.add(previewScroll, BorderLayout.CENTER);
        panel.add(form, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildTextAreaField(String label, JTextArea field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JLabel lbl = new JLabel(label);
        JScrollPane scroll = new JScrollPane(field);
        scroll.setPreferredSize(new Dimension(250, 120));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void loadSavedReminderValuesIntoFields() {
        Map<String, String> values = getDefaultReminderValues();

        try {
            values.putAll(templateService.loadValues(REMINDER_TEMPLATE_NAME));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load saved reminder values");
        }

        openingMessageField.setText(values.get("OPENING MESSAGE"));
        bodyField.setText(values.get("BODY"));
        endMessageField.setText(values.get("END MESSAGE"));
        signedField.setText(values.get("SIGNED"));
    }
    private void updateReminderPreview() {
        try {
            String template = templateService.loadTemplate(REMINDER_TEMPLATE_NAME);

            template = templateService.applyValues(template, getCurrentReminderFieldValues());
            template = templateService.applyValues(template, getReminderPreviewValues());

            reminderPreview.setText(template);
        } catch (IOException e) {
            reminderPreview.setText("Failed to load template");
        }
    }

    private Map<String, String> getReminderPreviewValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("CUSTACC", "ACC001");
        values.put("UNPAID", "£125.50");
        values.put("CLIENT", "Jane Smith");

        // harmless preview-only extras in case your real template includes them
        values.put("SECOND", "");
        values.put("INVOICENO", "42");
        values.put("BEGINMTH", "March");
        values.put("FIRSTORSECONDMESSAGE", "This is your first reminder regarding the unpaid balance on your account.");

        return values;
    }
    private void resetReminderToDefaults() {
        try {
            templateService.resetValues(REMINDER_TEMPLATE_NAME);

            Map<String, String> defaults = getDefaultReminderValues();
            openingMessageField.setText(defaults.get("OPENING MESSAGE"));
            bodyField.setText(defaults.get("BODY"));
            endMessageField.setText(defaults.get("END MESSAGE"));
            signedField.setText(defaults.get("SIGNED"));

            updateReminderPreview();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Reset failed");
        }
    }
    private Map<String, String> getCurrentReminderFieldValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("OPENING MESSAGE", openingMessageField.getText().trim());
        values.put("BODY", bodyField.getText().trim());
        values.put("END MESSAGE", endMessageField.getText().trim());
        values.put("SIGNED", signedField.getText().trim());
        return values;
    }
    private Map<String, String> getDefaultReminderValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("OPENING MESSAGE", "Our records show that your payment is now overdue.");
        values.put("BODY", "Please arrange payment as soon as possible to avoid further action.");
        values.put("END MESSAGE", "If you have already paid, please disregard this notice.");
        values.put("SIGNED", "Accounts Department");
        return values;
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
            templateService.ensureTemplateExists(REMINDER_TEMPLATE_NAME, DEFAULT_REMINDER_TEMPLATE);
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

    private void saveReminderValues() {
        try {
            templateService.saveValues(REMINDER_TEMPLATE_NAME, getCurrentReminderFieldValues());
            updateReminderPreview();
            JOptionPane.showMessageDialog(this, "Saved");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Save failed");
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