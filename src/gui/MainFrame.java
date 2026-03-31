package gui;
import service.LoginService;

import javax.swing.*;
import java.awt.*;

// Main JFrame with card view to flip between JPanels.
public class MainFrame extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;
    String user;
    Image backgroundImage;
    Image logoImage;
    LoginService loginService;

    public MainFrame() {
        loginService = new LoginService();

        try{
            backgroundImage = new ImageIcon("src/resources/gradient.png").getImage();
            logoImage = new ImageIcon("src/resources/logo.png").getImage();
        }
        catch(Exception e){
            System.out.println("no image load");
        }

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create JPanels
        LoginPage loginPage = new LoginPage(this, loginService);

        add(mainPanel);
        mainPanel.add(loginPage, "login");

        addPages(mainPanel);


        // Show login screen upon start
        getRootPane().setDefaultButton(loginPage.getLoginBtn()); // Login by pressing Enter
        cardLayout.show(mainPanel, "login");

        setTitle("IPOS CA");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Create panels and show dashboard upon login
    public void login(){
        addPages(mainPanel);
        cardLayout.show(mainPanel, "dashboard");
    }

    // Remove all panels and show login page
    public void logout(){
        mainPanel.removeAll();
        LoginPage loginPage = new LoginPage(this, loginService);
        mainPanel.add(loginPage, "login");
        getRootPane().setDefaultButton(loginPage.getLoginBtn());
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    void addPages(JPanel mainPanel) {
        Dashboard dashboard = new Dashboard(this);
        OrdersPage ordersPage = new OrdersPage(this);
        UsersPage usersPage = new UsersPage(this);
        CustomersPage customersPage = new CustomersPage(this);
        TemplatesPage templatesPage = new TemplatesPage(this);
        StockPage stockPage = new StockPage(this);
        SalesPage salesPage = new SalesPage(this);
        ReportsPage reportsPage = new ReportsPage(this);

        mainPanel.add(dashboard, "dashboard");
        mainPanel.add(ordersPage, "orders");
        mainPanel.add(usersPage, "users");
        mainPanel.add(customersPage, "customers");
        mainPanel.add(templatesPage, "templates");
        mainPanel.add(stockPage, "stock");
        mainPanel.add(salesPage, "sales");
        mainPanel.add(reportsPage, "reports");
    }

    //Changes the current page showing
    public void showPage(String name) {
        cardLayout.show(mainPanel, name);
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getUser(){
        return user;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public Image getLogoImage(){
        return logoImage;
    }

}
