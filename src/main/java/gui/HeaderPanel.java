package gui;

import service.AppController;
import service.TimeService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class HeaderPanel extends JPanel {

    private final JLabel timeLabel;
    private final TimeService timeService;

    public HeaderPanel(AppController appController) {

        this.timeService = appController.getTimeService();

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 50));

        //center
        ImageIcon logoIcon = new ImageIcon(
                appController.getLogo().getScaledInstance(150, 50, Image.SCALE_SMOOTH)
        );
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(logoLabel, BorderLayout.CENTER);

        //right time
        timeLabel = new JLabel();
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        updateTimeLabel();

        add(timeLabel, BorderLayout.EAST);
    }

    public void updateTimeLabel() {
        if (timeService.isVirtual()) {
            timeLabel.setText("Time (Virtual): (" + timeService.today() + ")");
        } else {
            timeLabel.setText("Time: (" + LocalDate.now() + ")");
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateTimeLabel();
    }
}