package gui;

import javax.swing.*;
import java.awt.*;

public class BottomBar extends JPanel {
    private JLabel loggedInLabel;
    private JButton logoutButton;

    public BottomBar(MainFrame mainFrame) {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(0, 40));

        loggedInLabel = new JLabel("Logged in as: " + mainFrame.getUser());
        logoutButton = new JButton("Logout");

        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(loggedInLabel, BorderLayout.WEST);
        add(logoutButton, BorderLayout.EAST);

        logoutButton.addActionListener(e -> {
            mainFrame.logout();
        });
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

}
