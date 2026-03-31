package gui;
import service.AppController;
import service.LoginService;

import javax.swing.*;
import java.awt.*;

public class LoginPage extends JPanel {
    private  ImageIcon logoIcon;
    private Image backgroundImage;
    private JButton loginBtn;

    public LoginPage(AppController appController, LoginService loginService) {
        //sorry broke logos and background, need to pass it from somewhere thats not mainframe

        setLayout(new BorderLayout());

        backgroundImage = appController.getBackground();

        // Outer container to handle positioning
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        loginBtn = new JButton("Login");
        JPanel form = new JPanel(new BorderLayout(10, 10));
        form.setPreferredSize(new Dimension(320, 180));
        form.setBackground(Color.WHITE);
        form.setOpaque(true);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        logoIcon = new ImageIcon(appController.getLogo().getScaledInstance(150, 50, Image.SCALE_SMOOTH));
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 10));
        fields.setOpaque(false);

        fields.add(new JLabel("Username:"));
        fields.add(userField);
        fields.add(new JLabel("Password:"));
        fields.add(passField);
        fields.add(new JLabel());
        fields.add(loginBtn);

        //form.add(logoLabel, BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);

        outer.add(form); // Add to outer container
        add(outer, BorderLayout.CENTER);



        // Add password logic here
        loginBtn.addActionListener(e -> {

            String entered_username = userField.getText();
            String entered_password = new String(passField.getPassword());

            if (loginService.authenticate(entered_username, entered_password)){
                appController.handleLogin(loginService.getLoggedInUser());
            } else{
                JOptionPane.showMessageDialog(this, "Incorrect username or password");
            }
        });
    }

    public JButton getLoginBtn() {
        return loginBtn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
