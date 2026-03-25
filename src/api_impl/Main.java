package api_impl;

import gui.LoginPage;
import gui.MainFrame;
import service.LoginService;

public class Main {
    //temporary to test gui
    public static void main(String[] args) {
        new MainFrame(new LoginService());
    }
}