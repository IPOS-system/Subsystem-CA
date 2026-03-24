package gui;
import javax.swing.*;
import java.awt.*;

//one j frame with card view flip between j panels.
public class MainFrame extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;
    String user;

    public MainFrame() {
        setTitle("IPOS CA");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        //add j panels
        mainPanel.add(new LoginPage(this), "login");

       // mainPanel.add(new DashboardHome(this), "dashboard");

        add(mainPanel);

        //what page to show right now
        cardLayout.show(mainPanel, "login");

        setVisible(true);
    }

    //performs actions on login
    public void login(){
        mainPanel.add(new Dashboard(this), "dashboard");
        mainPanel.add(new OrdersPage(this), "orders");
        cardLayout.show(mainPanel, "dashboard");
    }

    public void logout(){
        mainPanel.removeAll();
        mainPanel.add(new LoginPage(this), "login");
    }



    //function to change whats currently showing
    public void showPage(String name) {
        cardLayout.show(mainPanel, name);
    }

    public void setUser(String user){
        this.user = user;
    }
    public String getUser(){
        return user;
    }

}
