package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginService {

    public LoginService() {}


    public boolean authenticate(String entered_username, String entered_password){
        //boolean found_matching_account = false;
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
                        //found_matching_account = true; // if theyre the same then a matching account was found
                        return true;
                        //break;
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

}
