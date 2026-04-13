package gui;

import service.AppController;
import service.ReportService;
import domain.report.TurnoverReportEntry;
import domain.report.StockReportEntry;
import domain.report.DebtReportEntry;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * GUI panel shown when the manager clicks the *Reports* navigation button.
 *
 * Architecture follows the strict rule: UI → Service → DAO.
 */
public class ReportsPage extends JPanel {

    private final AppController controller;
    private final ReportService reportService;

    // UI components
    private final JComboBox<String> reportCombo;
    private final JTextField startField;
    private final JTextField endField;
    private final JButton generateBtn;
    private final JButton printBtn;
    private final JTextArea outputArea;

    public ReportsPage(AppController controller) {
        this.controller   = controller;
        this.reportService = controller.getReportService();   // <-- new getter

        setLayout(new BorderLayout());

        // ----- top controls -------------------------------------------------
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportCombo = new JComboBox<>(new String[]{
                "Turnover", "Stock Availability", "Debt Aggregation"
        });
        startField = new JTextField(10);
        endField   = new JTextField(10);
        generateBtn = new JButton("Generate");
        printBtn    = new JButton("Print");

        top.add(new JLabel("Report:"));
        top.add(reportCombo);
        top.add(new JLabel("Start (yyyy‑MM‑dd):"));
        top.add(startField);
        top.add(new JLabel("End (yyyy‑MM‑dd):"));
        top.add(endField);
        top.add(generateBtn);
        top.add(printBtn);
        add(top, BorderLayout.NORTH);

        // ----- output area -------------------------------------------------
        outputArea = new JTextArea();
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // ----- listeners ----------------------------------------------------
        generateBtn.addActionListener(e -> generateReport());
        printBtn.addActionListener(e -> printReport());

        // make date fields disabled when a report does not need them
        reportCombo.addActionListener(e -> toggleDateFields());
        toggleDateFields();
    }

    private void toggleDateFields() {
        boolean needDates = !"Stock Availability".equals(reportCombo.getSelectedItem());
        startField.setEnabled(needDates);
        endField.setEnabled(needDates);
    }

    private void generateReport() {
        String chosen = (String) reportCombo.getSelectedItem();
        try {
            switch (chosen) {
                case "Turnover" -> {
                    LocalDate s = LocalDate.parse(startField.getText().trim());
                    LocalDate e = LocalDate.parse(endField.getText().trim());
                    TurnoverReportEntry r = reportService.generateTurnoverReport(s, e);
                    outputArea.setText(reportService.formatTurnoverReport(r));
                }
                case "Stock Availability" -> {
                    List<StockReportEntry> list = reportService.generateStockReport();
                    outputArea.setText(reportService.formatStockReport(list));
                }
                case "Debt Aggregation" -> {
                    LocalDate s = LocalDate.parse(startField.getText().trim());
                    LocalDate e = LocalDate.parse(endField.getText().trim());
                    DebtReportEntry r = reportService.generateDebtReport(s, e);
                    outputArea.setText(reportService.formatDebtReport(r));
                }
                default -> outputArea.setText("Unknown report type");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error generating report:\n" + ex.getMessage(),
                    "Report error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printReport() {
        try {
            boolean ok = outputArea.print();
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Printing cancelled",
                        "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Printing failed:\n" + ex.getMessage(),
                    "Print error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
