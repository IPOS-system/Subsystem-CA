package gui;

import service.AppController;

import javax.swing.*;
import java.awt.*;

public class SalesPage extends JPanel {
    public SalesPage(AppController appController) {

        setLayout(new BorderLayout());
        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
    }

}