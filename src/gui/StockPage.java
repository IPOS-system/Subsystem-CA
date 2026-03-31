package gui;

import service.AppController;

import javax.swing.*;
import java.awt.*;

public class StockPage extends JPanel {
    public StockPage(AppController appController) {

        setLayout(new BorderLayout());
        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
    }
}
