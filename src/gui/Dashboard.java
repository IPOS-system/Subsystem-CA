package gui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JPanel {
    Image backgroundImage;

    public Dashboard(MainFrame mainFrame) {

        setLayout(new BorderLayout());

        backgroundImage =mainFrame.getBackgroundImage();

        add(new HeaderPanel(mainFrame), BorderLayout.NORTH);


        //center 3 region
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        mainPanel.setOpaque(false);

        //management area
        JPanel managementPanel = new JPanel(new BorderLayout());
        managementPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true), "Management"));
        managementPanel.setOpaque(false);

        JPanel managementButtons = new JPanel(new GridLayout(4, 1, 5, 5));
        managementButtons.setOpaque(false);

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
        salesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true), "Sales floor"));
        salesPanel.setOpaque(false);

        JPanel salesButtons = new JPanel(new GridLayout(1, 1));
        JButton salesBtn = new JButton("Sales");
        salesButtons.add(salesBtn);

        salesPanel.add(salesButtons, BorderLayout.NORTH);

        //administrative area
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true), "Administrative"));
        adminPanel.setOpaque(false);

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
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setOpaque(false);


        //alerts an empty j panel to have fun with/

        JPanel alertsPanel = new JPanel(new BorderLayout());
        alertsPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3, true), "Alerts"));
        alertsPanel.setOpaque(false);
        alertsPanel.setLayout(new BorderLayout());
        alertsPanel.setPreferredSize(new Dimension(0, 120));
        bottomPanel.add(alertsPanel, BorderLayout.NORTH);

        //status bar at the bottom "who is logged in" and logout.
        bottomPanel.add(new BottomBar(mainFrame));


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






    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

}