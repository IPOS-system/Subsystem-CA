package domain;

// flow for changing stuff
//change object in Java
//call DAO update method
//DAO sends UPDATE ... to DB
//DB becomes the real source of truth
//for fat tables, dont copy it all into an object? or do... not sure. fix

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;

    public User(int userId, String username, String password, String role) {
        this.username = username;
        this.role = role;
        this.password = password;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }//dao class update table
    public String getRole() {
        return role;
    }
    public String getPassword() {
        return password;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public int getUserId() {return userId;}
}
