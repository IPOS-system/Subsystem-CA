package gui;

import service.AppController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Dashboard extends JPanel {
    Image backgroundImage;

    public Dashboard(AppController appController) {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        backgroundImage = appController.getBackground();

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(makeMainPanel(appController), BorderLayout.CENTER);
        add(makeBottomArea(appController), BorderLayout.SOUTH);
    }

    private JPanel makeMainPanel(AppController appController) {
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        mainPanel.setOpaque(false);

        mainPanel.add(makeSection("Pharmacist",
                new JButton[]{
                        makeNavButton("Orders", "orders", appController),
                        makeNavButton("Customers", "customers", appController),
                        makeNavButton("Stock", "stock", appController),
                        makeNavButton("Sales", "sales", appController)
                }));

        mainPanel.add(makeSection("Manager",
                new JButton[]{
                        makeNavButton("Templates", "templates", appController),
                        makeNavButton("Reports", "reports", appController),
                        makeNavButton("Online Sales", "online", appController)
                }));

        mainPanel.add(makeSection("Administrator",
                new JButton[]{
                        makeNavButton("Users", "users", appController),
                        makeNavButton("Settings", "settings", appController)
                }));

        return mainPanel;
    }

    private JPanel makeSection(String title, JButton[] buttons) {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        buttonPanel.setOpaque(false);

        for (JButton button : buttons) {
            buttonPanel.add(button);
        }

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        section.add(titleLabel, BorderLayout.NORTH);
        section.add(buttonPanel, BorderLayout.CENTER);

        return section;
    }

    private JButton makeNavButton(String text, String page, AppController appController) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 42));
        button.addActionListener(e -> appController.showPage(page));
        return button;
    }

    private JPanel makeBottomArea(AppController appController) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel alertsPanel = new JPanel(new BorderLayout());
        alertsPanel.setOpaque(false);
        alertsPanel.setPreferredSize(new Dimension(0, 120));
        alertsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel alertsLabel = new JLabel("Alerts");
        alertsLabel.setFont(alertsLabel.getFont().deriveFont(Font.BOLD, 16f));
        alertsPanel.add(alertsLabel, BorderLayout.NORTH);

        wrapper.add(alertsPanel, BorderLayout.CENTER);
        wrapper.add(new BottomPanel(appController), BorderLayout.SOUTH);

        return wrapper;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}