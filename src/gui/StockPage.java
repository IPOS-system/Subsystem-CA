package gui;

import javax.swing.*;
import java.awt.*;

public class StockPage extends JPanel {
    public StockPage(MainFrame mainFrame) {

        setSize(800, 500);
        setLayout(new BorderLayout());
        setVisible(true);
        add(new BottomBar(mainFrame), BorderLayout.SOUTH);
    }
}
