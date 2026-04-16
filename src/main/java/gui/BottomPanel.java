package gui;

import service.AppController;

import javax.swing.*;
import java.awt.*;

public class BottomPanel extends JPanel {
    private JLabel loggedInLabel;
    private JButton logoutButton;
    private JButton dashboardButton;
    private JPanel rightPanel;

    public BottomPanel(AppController appController) {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(0, 40));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        //only run after login or crash
        String username = appController.getCurrentUser().getUsername();
        loggedInLabel = new JLabel("Logged in as: " + username);

        dashboardButton = new JButton("Dashboard");
        logoutButton = new JButton("Logout");

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(dashboardButton, BorderLayout.WEST);
        rightPanel.add(Box.createHorizontalStrut(10), BorderLayout.CENTER);
        rightPanel.add(logoutButton, BorderLayout.EAST);

        add(loggedInLabel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        dashboardButton.addActionListener(e -> {
            appController.showPage("dashboard");
        });

        logoutButton.addActionListener(e -> {
            appController.logout();
        });
    }
}
