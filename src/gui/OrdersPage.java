package gui;

import javax.swing.*;

import java.awt.*;

public class OrdersPage extends JPanel {

    public OrdersPage(MainFrame mainFrame) {

        setLayout(new BorderLayout());
        add(new HeaderPanel(mainFrame), BorderLayout.NORTH);
        add(new BottomPanel(mainFrame), BorderLayout.SOUTH);
    }
}