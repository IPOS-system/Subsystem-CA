package gui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private Image backgroundImage;
    private Image logoImage;


    public MainFrame() {

        try {
            ClassLoader cl = getClass().getClassLoader();

            URL bgUrl = cl.getResource("gradient.png");
            URL logoUrl = cl.getResource("cosymedlogo.png");

            if (bgUrl != null) {
                backgroundImage = new ImageIcon(bgUrl).getImage();
            } else {
                System.out.println("gradient.png not found");
            }

            if (logoUrl != null) {
                logoImage = new ImageIcon(logoUrl).getImage();
            } else {
                System.out.println("logo.png not found");
            }

        } catch (Exception e) {
            System.out.println("no image load");
        }

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        add(mainPanel);

        setTitle("IPOS CA");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void addPage(String name, JPanel page) {
        mainPanel.add(page, name);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void clearPages() {
        mainPanel.removeAll();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showPage(String name) {
        cardLayout.show(mainPanel, name);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void setEnterButton(JButton button) {
        getRootPane().setDefaultButton(button);
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public Image getLogoImage() {
        return logoImage;
    }
}