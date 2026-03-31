package gui;

import service.AppController;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    private final ImageIcon logoIcon;

    public HeaderPanel(AppController appController) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setPreferredSize(new Dimension(0, 50));

        logoIcon = new ImageIcon(appController.getLogo().getScaledInstance(150, 50, Image.SCALE_SMOOTH));
        JLabel logoLabel = new JLabel(logoIcon);
        add(logoLabel);
    }
}