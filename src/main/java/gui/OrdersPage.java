package gui;

import service.AppController;

import javax.swing.*;

import java.awt.*;

public class OrdersPage extends JPanel {

    public OrdersPage(AppController appController) {

        setLayout(new BorderLayout());
        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
    }
}