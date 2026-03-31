package gui;

import service.AppController;

import javax.swing.*;
import java.awt.*;

public class UsersPage extends JPanel {


    public UsersPage(AppController appController) {
        setLayout(new BorderLayout());

        // top
        add(new HeaderPanel(appController), BorderLayout.NORTH);

    }
}
