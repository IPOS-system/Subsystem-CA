package gui;

import domain.User;
import service.AppController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Dashboard extends JPanel {
    private final AppController appController;
    Image backgroundImage;

    public Dashboard(AppController appController) {
        this.appController = appController;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        backgroundImage = appController.getBackground();

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(makeMainPanel(), BorderLayout.CENTER);
        add(makeBottomArea(), BorderLayout.SOUTH);
    }

    private JPanel makeMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 12, 12));
        mainPanel.setOpaque(false);

        mainPanel.add(makeSection("Pharmacist",
                new JButton[]{
                        makeNavButton("Orders", "orders"),
                        makeNavButton("Customers", "customers"),
                        makeNavButton("Stock", "stock"),
                        makeNavButton("Sales", "sales")
                }));

        mainPanel.add(makeSection("Manager",
                new JButton[]{
                        makeNavButton("Templates", "templates"),
                        makeNavButton("Reports", "reports"),
                        makeNavButton("Online Sales", "online")
                }));

        mainPanel.add(makeSection("Administrator",
                new JButton[]{
                        makeNavButton("Users", "users"),
                        makeNavButton("Settings", "settings")
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

    private JButton makeNavButton(String text, String page) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.addActionListener(e -> guardedShowPage(page));
        return button;
    }

    private void guardedShowPage(String pageName) {
        if (!hasAccess(pageName)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Access denied. You do not have permission to open this page."
            );
            return;
        }

        appController.showPage(pageName);
    }

    // Manages access to each page depending on user role
    private boolean hasAccess(String pageName) {
        User user = appController.getCurrentUser();
        if (user == null) return false;

        String role = user.getRole();

        return switch (pageName) {

            // Pharmacist access
            case "orders", "customers", "stock", "sales" -> role.equals("Pharmacist") || role.equals("Manager") || role.equals("Director of Operations/Manager") || role.equals("Full Access") ;

            // Manager access
            case "templates", "reports", "online" -> role.equals("Manager") || role.equals("Director of Operations/Manager")
                    || role.equals("Accountant") || role.equals("Senior accountant")  || role.equals("Full Access");

            // Admin access
            case "users" -> role.equals("Administrator") || role.equals("Full Access");

            // All roles have access to settings
            case "settings" -> true;

            default -> false;
        };

    }

    private JPanel makeBottomArea() {
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