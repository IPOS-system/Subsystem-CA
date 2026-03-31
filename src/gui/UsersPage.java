package gui;

import dao.UserDAO;
import domain.User;
import service.AppController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersPage extends JPanel {


    private final AppController appController;
    private final UserDAO userDAO;


    public UsersPage(AppController appController) {
        this.appController = appController;
        this.userDAO = new UserDAO();

    }

}