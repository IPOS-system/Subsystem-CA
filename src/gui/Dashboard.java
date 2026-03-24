package gui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JPanel {

    public Dashboard(MainFrame mainFrame) {

        setLayout(new BorderLayout());

        //top region
        JPanel topPanel = new JPanel(null);
        topPanel.setPreferredSize(new Dimension(0, 80));

        JLabel logoLabel = new JLabel("LOGO");
        logoLabel.setBounds(330, 20, 200, 40);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));

        topPanel.add(logoLabel);
        add(topPanel, BorderLayout.NORTH);

        //center 3 region
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        //management area
        JPanel managementPanel = new JPanel(new BorderLayout());
        managementPanel.setBorder(BorderFactory.createTitledBorder("Management"));

        JPanel managementButtons = new JPanel(new GridLayout(4, 1, 5, 5));

        JButton ordersBtn = new JButton("Orders");
        managementButtons.add(ordersBtn);

        JButton stockBtn = new JButton("Stock");
        managementButtons.add(stockBtn);

        JButton accountBtn = new JButton("Accounts");
        managementButtons.add(accountBtn);

        JButton reportsBtn = new JButton("Reports");
        managementButtons.add(reportsBtn);

        managementPanel.add(managementButtons, BorderLayout.NORTH);

        //sales floor
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesPanel.setBorder(BorderFactory.createTitledBorder("Sales Floor"));

        JPanel salesButtons = new JPanel(new GridLayout(1, 1));
        JButton salesBtn = new JButton("Sales");
        salesButtons.add(salesBtn);

        salesPanel.add(salesButtons, BorderLayout.NORTH);

        //administrative area
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBorder(BorderFactory.createTitledBorder("Admin"));

        JPanel adminButtons = new JPanel(new GridLayout(1, 1));
        adminButtons.add(new JButton("Templates"));

        adminPanel.add(adminButtons, BorderLayout.NORTH);

        //add columns
        mainPanel.add(managementPanel);
        mainPanel.add(salesPanel);
        mainPanel.add(adminPanel);

        add(mainPanel, BorderLayout.CENTER);

        //bottom region

        JPanel bottomPanel = new JPanel(new BorderLayout());

        BottomBar bottomBar = new BottomBar(mainFrame);
        bottomPanel.add(bottomBar);

        add(bottomPanel, BorderLayout.SOUTH);

        //alerts an empty j panel to have fun with/

        JPanel alertsPanel = new JPanel(new BorderLayout());
        alertsPanel.setBorder(BorderFactory.createTitledBorder("Alerts"));
        alertsPanel.setLayout(new BorderLayout());
        alertsPanel.setPreferredSize(new Dimension(0, 120));
        bottomPanel.add(alertsPanel, BorderLayout.NORTH);

        //status bar at the bottom "who is logged in" and logout.

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setPreferredSize(new Dimension(0, 30));

        //who is logged in label

        JLabel userLabel = new JLabel("Logged in as: admin");
        statusBar.add(userLabel, BorderLayout.WEST);

        //navigation

        ordersBtn.addActionListener(e -> {
            mainFrame.showPage("orders");
        });

        stockBtn.addActionListener(e -> {
            mainFrame.showPage("stock");
        });

        accountBtn.addActionListener(e -> {
            mainFrame.showPage("accounts");
        });
        reportsBtn.addActionListener(e -> {
            mainFrame.showPage("reports");
        });

        salesBtn.addActionListener(e -> {
            mainFrame.showPage("sales");
        });


        bottomBar.getLogoutButton().addActionListener(e -> {
            mainFrame.showPage("login");
        });



    }
}