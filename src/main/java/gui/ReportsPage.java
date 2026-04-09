package gui;

import service.AppController;

import javax.swing.*;
import java.awt.*;

public class ReportsPage extends JPanel {
    public ReportsPage(AppController appController) {

        setLayout(new BorderLayout());
        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
    }
}
