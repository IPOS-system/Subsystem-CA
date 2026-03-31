package service;

import datastorage.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginService {
    private User LoggedinUser;

    public LoginService() {}
    public boolean authenticate(String entered_username, String entered_password){
        try {
            FileReader fr = new FileReader("src/datastorage/AccountDetails");
            BufferedReader br = new BufferedReader(fr);
            String line;
            boolean foundstart = false;
            while ((line = br.readLine()) != null) { // Search each line in the database for the account
                if (!foundstart) {
                    if (line.equals("start")) {
                        foundstart = true;
                    } else {
                        continue;
                    }
                }
                String[] parts = line.split(","); // split the string at the commas

                if (parts.length >= 2) {
                    String username_to_check = parts[0]; // Username
                    String password_to_check = parts[1]; // Password

                    if (username_to_check.equals(entered_username) && password_to_check.equals(entered_password)) { // Validate username and password
                        String role_of_user = parts[2];
                        LoggedinUser = new User(entered_username, role_of_user);
                        return true; // Matching account found
                    }
                }
            }
        }
        catch (IOException exception){
            System.out.println("Error reading file");
        }
        return false; // No matching account found
    }
    public User getLoggedinUser() {
        return LoggedinUser;
    }

}
