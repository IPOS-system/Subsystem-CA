package gui;

import javax.swing.*;
import java.awt.*;

public class ReportsPage extends JPanel {
    public ReportsPage(MainFrame mainFrame) {

        setLayout(new BorderLayout());
        add(new HeaderPanel(mainFrame), BorderLayout.NORTH);
        add(new BottomPanel(mainFrame), BorderLayout.SOUTH);
    }
}
