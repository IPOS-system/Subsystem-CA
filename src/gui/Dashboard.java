package gui;

import service.AppController;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JPanel {
    Image backgroundImage;

    public Dashboard(AppController appController) {

        setLayout(new BorderLayout());

        backgroundImage = appController.getBackground();

        add(new HeaderPanel(appController), BorderLayout.NORTH);
       // add(new BottomPanel(appController), BorderLayout.SOUTH);

        // Main panel used for dashboard
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.setOpaque(false);

        // Pharmacist Area
        JPanel pharmacistPanel = new JPanel(new BorderLayout());
        pharmacistPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true), "Pharmacist"));
        pharmacistPanel.setOpaque(false);

        JPanel pharmacistButtons = new JPanel(new GridLayout(4, 1, 5, 5));
        pharmacistButtons.setOpaque(false);

        JButton ordersButton = new JButton("Orders");
        JButton customersButton = new JButton("Customers");
        JButton stockButton = new JButton("Stock");
        JButton salesButton = new JButton("Sales");

        pharmacistButtons.add(ordersButton);
        pharmacistButtons.add(customersButton);
        pharmacistButtons.add(stockButton);
        pharmacistButtons.add(salesButton);

        pharmacistPanel.add(pharmacistButtons, BorderLayout.NORTH);

        // Manager Area
        JPanel managerPanel = new JPanel(new BorderLayout());
        managerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true), "Manager"));
        managerPanel.setOpaque(false);

        JPanel managerButtons = new JPanel(new GridLayout(2, 1, 5, 5));
        managerButtons.setOpaque(false);

        JButton templatesButton = new JButton("Templates");
        JButton reportsButton = new JButton("Reports");

        managerButtons.add(templatesButton);
        managerButtons.add(reportsButton);

        managerPanel.add(managerButtons, BorderLayout.NORTH);

        // Admin Area
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true), "Administrator"));
        adminPanel.setOpaque(false);

        JPanel adminButtons = new JPanel(new GridLayout(1, 1, 5, 5));
        adminButtons.setOpaque(false);

        JButton usersButton = new JButton("Users");
        adminButtons.add(usersButton);

        adminPanel.add(adminButtons, BorderLayout.NORTH);

        // Add columns
        mainPanel.add(pharmacistPanel);
        mainPanel.add(managerPanel);
        mainPanel.add(adminPanel);
        add(mainPanel, BorderLayout.CENTER);

        // Local bottom panel to host alerts panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(new BottomPanel(appController));
        bottomPanel.setOpaque(false);

        // Alerts Area to show important alerts and information
        JPanel alertsPanel = new JPanel(new BorderLayout());
        alertsPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true), "Alerts"));
        alertsPanel.setOpaque(false);
        alertsPanel.setLayout(new BorderLayout());
        alertsPanel.setPreferredSize(new Dimension(0, 120));
        bottomPanel.add(alertsPanel, BorderLayout.NORTH);

        //Bind panels to buttons
        ordersButton.addActionListener(e -> {
            appController.showPage("orders");
        });

        customersButton.addActionListener(e -> {
            appController.showPage("customers");
        });

        stockButton.addActionListener(e -> {
            appController.showPage("stock");
        });

        salesButton.addActionListener(e -> {
            appController.showPage("sales");
        });

        templatesButton.addActionListener(e -> {
            appController.showPage("templates");
        });

        reportsButton.addActionListener(e -> {
            appController.showPage("reports");
        });

        usersButton.addActionListener(e -> {
            appController.showPage("users");
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

}