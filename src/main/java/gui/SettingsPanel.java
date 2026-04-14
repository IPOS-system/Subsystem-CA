package gui;

import service.AccountStatusService;
import service.AppController;
import service.TimeService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class SettingsPanel extends JPanel {

    private final AppController controller;
    private final TimeService timeService;
    private final AccountStatusService accountStatusService;

    private JLabel modeLabel;
    private JSpinner dateSpinner;

    public SettingsPanel(AppController controller, AccountStatusService accountStatusService) {
        this.controller = controller;
        this.timeService = controller.getTimeService();
        this.accountStatusService = accountStatusService;

        setLayout(new BorderLayout());

        add(new HeaderPanel(controller), BorderLayout.NORTH);
        add(new BottomPanel(controller), BorderLayout.SOUTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Time Settings");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));

        SpinnerDateModel model = new SpinnerDateModel();
        dateSpinner = new JSpinner(model);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        JButton todayBtn = new JButton("Today");
        JButton plus7Btn = new JButton("+7 days");
        JButton plus30Btn = new JButton("+30 days");
        JButton applyBtn = new JButton("Apply");
        JButton resetBtn = new JButton("Reset");

        row.add(new JLabel("Virtual Date:"));
        row.add(dateSpinner);
        row.add(todayBtn);
        row.add(plus7Btn);
        row.add(plus30Btn);
        row.add(applyBtn);
        row.add(resetBtn);

        panel.add(row);

        panel.add(Box.createVerticalStrut(20));

        modeLabel = new JLabel();
        updateLabel();
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(modeLabel);

        todayBtn.addActionListener(e -> setSpinner(LocalDate.now()));
        plus7Btn.addActionListener(e -> adjustDays(7));
        plus30Btn.addActionListener(e -> adjustDays(30));

        applyBtn.addActionListener(e -> {
            LocalDate selected = getSpinnerDate();
            timeService.setVirtualDate(selected);
            accountStatusService.refreshStatuses(selected); //refreshhhh

            updateLabel();
        });

        resetBtn.addActionListener(e -> {
            timeService.reset();
            updateLabel();
        });

        return panel;
    }

    private void adjustDays(int days) {
        LocalDate current = getSpinnerDate();
        setSpinner(current.plusDays(days));
    }

    private void setSpinner(LocalDate date) {
        Date d = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dateSpinner.setValue(d);
    }

    private LocalDate getSpinnerDate() {
        Date d = (Date) dateSpinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void updateLabel() {
        if (timeService.isVirtual()) {
            modeLabel.setText("Mode: Virtual (" + timeService.today() + ")");
        } else {
            modeLabel.setText("Mode: Real Time (" + LocalDate.now() + ")");
        }
    }
}