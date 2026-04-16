package gui;

import service.AppController;
import service.ReportService;
import domain.report.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ReportsPage extends JPanel {

    private final AppController controller;
    private final ReportService reportService;

    private JComboBox<String> reportCombo;
    private JComboBox<String> rangeCombo;
    private JTextField startField;
    private JTextField endField;
    private JTextArea outputArea;

    public ReportsPage(AppController controller) {
        this.controller = controller;
        this.reportService = controller.getReportService();

        setLayout(new BorderLayout());

        add(new HeaderPanel(controller), BorderLayout.NORTH);
        add(new BottomPanel(controller), BorderLayout.SOUTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

        reportCombo = new JComboBox<>(new String[]{
                "Turnover", "Stock Availability", "Debt Aggregation"
        });

        rangeCombo = new JComboBox<>(new String[]{
                "Today", "Last 7 Days", "Last 30 Days", "Custom"
        });

        startField = new JTextField(10);
        endField = new JTextField(10);

        JButton generateBtn = new JButton("Generate");
        JButton printBtn = new JButton("Print");

        controls.add(new JLabel("Report:"));
        controls.add(reportCombo);
        controls.add(new JLabel("Range:"));
        controls.add(rangeCombo);
        controls.add(new JLabel("Start:"));
        controls.add(startField);
        controls.add(new JLabel("End:"));
        controls.add(endField);
        controls.add(generateBtn);
        controls.add(printBtn);

        outputArea = new JTextArea();
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setEditable(false);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        generateBtn.addActionListener(e -> generateReport());
        printBtn.addActionListener(e -> printReport());
        rangeCombo.addActionListener(e -> applyPreset());
        reportCombo.addActionListener(e -> toggleDateFields());

        applyPreset();
        toggleDateFields();

        return panel;
    }

    private void applyPreset() {
        String selected = (String) rangeCombo.getSelectedItem();
        LocalDate today = controller.getTimeService().today();

        switch (selected) {
            case "Today" -> {
                startField.setText(today.toString());
                endField.setText(today.toString());
                startField.setEnabled(false);
                endField.setEnabled(false);
            }
            case "Last 7 Days" -> {
                startField.setText(today.minusDays(7).toString());
                endField.setText(today.toString());
                startField.setEnabled(false);
                endField.setEnabled(false);
            }
            case "Last 30 Days" -> {
                startField.setText(today.minusDays(30).toString());
                endField.setText(today.toString());
                startField.setEnabled(false);
                endField.setEnabled(false);
            }
            case "Custom" -> {
                startField.setEnabled(true);
                endField.setEnabled(true);
            }
        }
    }

    private void toggleDateFields() {
        boolean needDates = !"Stock Availability".equals(reportCombo.getSelectedItem());
        rangeCombo.setEnabled(needDates);
        startField.setEnabled(needDates && "Custom".equals(rangeCombo.getSelectedItem()));
        endField.setEnabled(needDates && "Custom".equals(rangeCombo.getSelectedItem()));
    }

    private void generateReport() {
        String chosen = (String) reportCombo.getSelectedItem();

        try {
            LocalDate start = null;
            LocalDate end = null;

            if (!"Stock Availability".equals(chosen)) {
                start = LocalDate.parse(startField.getText().trim());
                end = LocalDate.parse(endField.getText().trim());

                if (start.isAfter(end)) {
                    throw new IllegalArgumentException("Start date must be before end date.");
                }

                if (end.isAfter(controller.getTimeService().today())) {
                    throw new IllegalArgumentException("End date cannot be in the future.");
                }
            }

            switch (chosen) {
                case "Turnover" -> {
                    TurnoverReportEntry r = reportService.generateTurnoverReport(start, end);
                    outputArea.setText(reportService.formatTurnoverReport(r));
                }
                case "Stock Availability" -> {
                    List<StockReportEntry> list = reportService.generateStockReport();
                    outputArea.setText(reportService.formatStockReport(list));
                }
                case "Debt Aggregation" -> {
                    DebtReportEntry r = reportService.generateDebtReport(start, end);
                    outputArea.setText(reportService.formatDebtReport(r));
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReport() {
        try {
            if (!outputArea.print()) {
                JOptionPane.showMessageDialog(this,
                        "Printing cancelled.",
                        "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Printing error.", JOptionPane.ERROR_MESSAGE);
        }
    }
}