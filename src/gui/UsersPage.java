package gui;

import javax.swing.*;
import java.awt.*;

public class UsersPage extends JPanel {
    public UsersPage(MainFrame mainFrame) {

        setLayout(new BorderLayout());
        add(new HeaderPanel(mainFrame), BorderLayout.NORTH);
        add(new BottomPanel(mainFrame), BorderLayout.SOUTH);
    }
}
