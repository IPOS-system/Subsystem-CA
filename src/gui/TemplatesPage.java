package gui;

import javax.swing.*;
import java.awt.*;

public class TemplatesPage extends JPanel {
    public TemplatesPage(MainFrame mainFrame) {

        setLayout(new BorderLayout());
        add(new HeaderPanel(mainFrame), BorderLayout.NORTH);
        add(new BottomPanel(mainFrame), BorderLayout.SOUTH);
    }
}
