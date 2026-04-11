package api_impl;

import gui.MainFrame;
import service.*;
import gui.*;
import domain.*;
import dao.*;
import api_impl.*;
import api.*;


import javax.swing.*;
import java.awt.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {


    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        //START SPRING FIRST
        SpringApplication.run(Main.class, args);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.put("Button.background", new Color(60, 120, 200));
            UIManager.put("Button.foreground", Color.white);
            UIManager.put("Button.focus", new Color(60, 120, 200));
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("OptionPane.background", Color.WHITE);
            UIManager.put("Viewport.background", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseSetup.initialiseDatabase();

        LoginService loginService = new LoginService();
        Session session = new Session();
        MainFrame mainFrame = new MainFrame();
        SaleService saleService = new SaleService();
        CustomerService customerService = new CustomerService();
        ItemService itemService = new ItemService();

        AppController appController = new AppController(
                mainFrame, loginService, session,
                customerService, itemService, saleService
        );

        appController.start();
    }
}