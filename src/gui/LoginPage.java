package gui;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class LoginPage extends JPanel {
    private Image backgroundImage;


    public LoginPage(MainFrame mainFrame) {

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
            // get entered info
            String entered_username = userField.getText();
            String entered_password = new String(passField.getPassword());

            // validation
            if (entered_username.isEmpty()) {
                return; // username was not entered
            }
            else if (entered_password.isEmpty()) {
                return; // password was not entered
            }
            boolean found_matching_account = false;
            try {
                FileReader fr = new FileReader("src/datastorage/AccountDetails");
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) { // go through each account in the database, or each line in the file
                    String[] parts = line.split(","); // split the string at the commas

                    if (parts.length >= 2) {
                        String username_to_check = parts[0]; // first word in the split string is the username of the account
                        String password_to_check = parts[1]; // second word is the password

                        if (username_to_check.equals(entered_username) && password_to_check.equals(entered_password)) { // compare them with the username and password that were typed
                            found_matching_account = true; // if theyre the same then a matching account was found
                            break;
                        }
                    }
                }
            }
            catch (IOException exception){
                exception.printStackTrace();
            }
            if (!found_matching_account) { return; } // did not find an account with the given details in the entire file (meaning username or password is incorrect)

            // if program continues to here then the username and password were valid
            // set displayed account username and finish logging in
            mainFrame.setUser(entered_username);
            mainFrame.login();
        });
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}
