package service;

import datastorage.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginService {
    private User LoggedinUser;

    public LoginService() {}
    public boolean authenticate(String entered_username, String entered_password){
        //boolean found_matching_account = false;
        try {
            FileReader fr = new FileReader("src/datastorage/AccountDetails");
            BufferedReader br = new BufferedReader(fr);
            String line;
            boolean foundstart = false;
            while ((line = br.readLine()) != null) { // go through each account in the database, or each line in the file
                if (!foundstart) {
                    if (line.equals("start")) {
                        foundstart = true;
                    } else {
                        continue;
                    }
                }
                String[] parts = line.split(","); // split the string at the commas

                if (parts.length >= 2) {
                    String username_to_check = parts[0]; // first word in the split string is the username of the account
                    String password_to_check = parts[1]; // second word is the password

                    if (username_to_check.equals(entered_username) && password_to_check.equals(entered_password)) { // compare them with the username and password that were typed
                        String role_of_user = parts[2];
                        LoggedinUser = new User(entered_username, role_of_user);
                        return true; // if theyre the same then a matching account was found
                    }
                }
            }
        }
        catch (IOException exception){
            System.out.println("Error reading file");
            exception.printStackTrace();
        }
        //if (!found_matching_account) { return false; } // did not find an account with the given details in the entire file (meaning username or password is incorrect)
        return false;
    }
    public User getLoggedinUser() {
        return LoggedinUser;
    }

}
