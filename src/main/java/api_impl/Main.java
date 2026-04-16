package api_impl;

import gui.MainFrame;
import service.*;
import gui.*;
import domain.*;
import dao.*;
import api_impl.*;
import api.*;


import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.sql.Time;

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
            setGlobalFont(new Font("SansSerif", Font.PLAIN, 13));


        } catch (Exception e) {
            e.printStackTrace();
        }



        DatabaseSetup.initialiseDatabase();

        LoginService loginService = new LoginService();

        Session session = new Session();

        TimeService timeService = new TimeService();
        AccountStatusService accountStatusService = new AccountStatusService();
        MainFrame mainFrame = new MainFrame();
        DebtService debtService = new DebtService(timeService, accountStatusService);

        SAService saService = new SAService();



        CustomerService customerService = new CustomerService(debtService);
        PaymentService paymentService = new PaymentService(debtService);
        SaleService saleService = new SaleService(customerService, paymentService,timeService, debtService,saService);
        ItemService itemService = new ItemService();
        SaleService orderService = new SaleService(customerService, paymentService,timeService, debtService, saService);
        TemplateService templateService = new TemplateService();
        CatalogueService catalogueService = new CatalogueService(saService);



        AppController appController = new AppController(
                mainFrame, loginService, session,
                customerService, itemService, saleService,
                orderService, templateService, catalogueService,
                timeService, accountStatusService, paymentService,
                saService
        );

        //appController.getTemplateService().syncTemplatesWithFilesystem();

        appController.start();
    }
    public static void setGlobalFont(Font font) {
        FontUIResource f = new FontUIResource(font);
        UIManager.put("Button.font", f);
        UIManager.put("Label.font", f);
        UIManager.put("Panel.font", f);
        UIManager.put("TabbedPane.font", f);
        UIManager.put("Table.font", f);
        UIManager.put("TableHeader.font", f);
        UIManager.put("TextField.font", f);
        UIManager.put("PasswordField.font", f);
        UIManager.put("TextArea.font", f);
        UIManager.put("ComboBox.font", f);
        UIManager.put("CheckBox.font", f);
        UIManager.put("RadioButton.font", f);
        UIManager.put("Menu.font", f);
        UIManager.put("MenuItem.font", f);
        UIManager.put("TitledBorder.font", f);
    }
}