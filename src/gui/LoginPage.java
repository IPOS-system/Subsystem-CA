package gui;
import service.LoginService;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class LoginPage extends JPanel {
    private Image backgroundImage;


    public LoginPage(MainFrame mainFrame, LoginService loginService) {

        //add logo
        backgroundImage =mainFrame.getBackgroundImage();

        setLayout(new BorderLayout());
        //outer container to handle centering

        JPanel outer = new JPanel(new GridBagLayout());

        outer.setOpaque(false);

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");
        JPanel form = new JPanel(new BorderLayout(10, 10));
        form.setPreferredSize(new Dimension(320, 180));
        form.setBackground(Color.WHITE);
        form.setOpaque(true);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        ImageIcon originalIcon = new ImageIcon("src/resources/logo.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(120, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel logoLabel = new JLabel(scaledIcon);

        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 10));
        fields.setOpaque(false);

        fields.add(new JLabel("Username:"));
        fields.add(userField);
        fields.add(new JLabel("Password:"));
        fields.add(passField);
        fields.add(new JLabel());
        fields.add(loginBtn);

        form.add(logoLabel, BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);

        outer.add(form); //add to outer

        add(outer, BorderLayout.CENTER);




        //add password logic here
        loginBtn.addActionListener(e -> {

            String entered_username = userField.getText();
            String entered_password = new String(passField.getPassword());

            if(loginService.authenticate(entered_username, entered_password)){
                mainFrame.setUser(entered_username);
                mainFrame.login();
            }
            else{
                JOptionPane.showMessageDialog(mainFrame, "Incorrect username or password");
            }

        });
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
