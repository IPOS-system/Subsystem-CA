package gui;

import javax.swing.*;
import java.awt.*;

public class BottomPanel extends JPanel {
    private JLabel loggedInLabel;
    private JButton logoutButton;
    private JButton dashboardButton;
    private JPanel rightPanel;

    public BottomPanel(MainFrame mainFrame) {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(0, 40));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        loggedInLabel = new JLabel("Logged in as: " + mainFrame.getUser().getUsername());

        dashboardButton = new JButton("Dashboard");
        logoutButton = new JButton("Logout");

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(dashboardButton, BorderLayout.WEST);
        rightPanel.add(logoutButton, BorderLayout.EAST);

        add(loggedInLabel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);

        dashboardButton.addActionListener(e -> {
            mainFrame.showPage("dashboard");
        });

        logoutButton.addActionListener(e -> {
            mainFrame.logout();
        });
    }
}
