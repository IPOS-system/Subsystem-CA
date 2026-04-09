package service;

import dao.UserDAO;
import domain.User;

public class LoginService {
    private final UserDAO userDAO;

    public LoginService() {
        //make dao object
        this.userDAO = new UserDAO();
    }

    public User authenticate(String entered_username, String entered_password) {
        //find user by username
        //creates a user object, if this returns false GC just trashes it.
        //else it stores it, passes it back to mainframe to maintain logged in user.
        User user = userDAO.findByUsername(entered_username);

        //if user does not exist, fail login
        if (user == null) {
            return null;
        }

        //check password
        //TODO if password is ever null -> nullpointer exception.
        if (user.getPassword().equals(entered_password)) {
            //save logged in user
            return user;
        }

        //wrong password

        return null;
    }

}