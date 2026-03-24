package gui;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    private final ImageIcon logoIcon;

    public HeaderPanel(MainFrame mainFrame) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setPreferredSize(new Dimension(0, 50));


        logoIcon = new ImageIcon(mainFrame.getLogoImage().getScaledInstance(100, 50, Image.SCALE_SMOOTH));

        JLabel logoLabel = new JLabel(logoIcon);

        add(logoLabel);
    }
}