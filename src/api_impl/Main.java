package api_impl;

import gui.MainFrame;
import service.AppController;
import service.LoginService;
import service.Session;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            //all this just makes it look nice
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.put("Button.background", new Color(60, 120, 200)); //
            UIManager.put("Button.foreground", Color.white);
            UIManager.put("Button.focus", new Color(60, 120, 200)); //
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("OptionPane.background", Color.WHITE);
            UIManager.put("Viewport.background", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DatabaseSetup.initialiseDatabase();
        //technically database setup should run only once, but whatever.
        LoginService loginService = new LoginService();
        Session session = new Session();
        MainFrame mainFrame = new MainFrame();
        AppController appController = new AppController(mainFrame, loginService, session);
        appController.start();

    }
}