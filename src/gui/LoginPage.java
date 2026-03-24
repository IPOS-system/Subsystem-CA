package gui;
import javax.swing.*;
import java.awt.*;

public class LoginPage extends JPanel {

    public LoginPage(MainFrame mainFrame) {

        //add logo

        setLayout(new BorderLayout());

        //outer container to handle centering
        JPanel outer = new JPanel(new GridBagLayout());

        //inner login box
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setPreferredSize(new Dimension(300, 120));//Fixed size

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        form.add(new JLabel("Username:"));
        form.add(userField);
        form.add(new JLabel("Password:"));
        form.add(passField);
        form.add(new JLabel());
        form.add(loginBtn);

        outer.add(form); //add to outer

        add(outer, BorderLayout.CENTER);

        //add password logic here
        loginBtn.addActionListener(e -> {
            mainFrame.setUser(userField.getText());
            mainFrame.login();

        });
    }
}
