package gui;

import javax.swing.*;

import java.awt.*;

public class OrdersPage extends JPanel {

    public OrdersPage(MainFrame mainframe) {
        setLayout(new BorderLayout());

        add(new BottomBar(mainframe), BorderLayout.SOUTH);
    }
}