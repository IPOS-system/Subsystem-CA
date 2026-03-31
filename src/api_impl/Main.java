package api_impl;

import gui.MainFrame;
import service.AppController;
import service.LoginService;
import service.Session;

public class Main {
    public static void main(String[] args) {
        DatabaseSetup.initialiseDatabase();
        //technically database setup should run only once, but whatever.
        LoginService loginService = new LoginService();
        Session session = new Session();
        MainFrame mainFrame = new MainFrame();
        AppController appController  =new AppController(mainFrame, loginService, session);
        appController.start();

    }
}