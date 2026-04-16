package service;

import domain.User;

public class Session {
    private User currentUser;
    private String role;

    public void setCurrentUser(User user) {
        this.currentUser = user;

    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }



}
