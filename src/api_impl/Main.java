package api_impl;

import gui.MainFrame;

public class Main {
    public static void main(String[] args) {
        DatabaseSetup.initialiseDatabase();
        new MainFrame();
    }
}