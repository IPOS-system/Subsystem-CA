package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class SalesPage extends JPanel {
    public SalesPage(MainFrame mainFrame) {

        setSize(800, 500);
        setLayout(new BorderLayout());

        add(new BottomBar(mainFrame), BorderLayout.SOUTH);

    }

}