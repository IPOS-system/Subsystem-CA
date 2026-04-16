package gui;

import dao.UserDAO;
import domain.User;
import service.AppController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersPage extends JPanel {


    private final AppController app;
    private final UserDAO usersDao;

    //table
    private JTable tbl;
    private DefaultTableModel model;

    //form
    private JTextField userTxt;
    private JPasswordField passTxt;
    private JComboBox<String> roleDrop;

    //buttons
    private JButton addBtn;
    private JButton updBtn;
    private JButton delBtn;
    private JButton clrBtn;

    public UsersPage(AppController appController) {
        this.app = appController;
        this.usersDao = new UserDAO();

        setLayout(new BorderLayout());

        //header/footer are shared panels
        add(new HeaderPanel(app), BorderLayout.NORTH);
        add(makeMiddle(), BorderLayout.CENTER);
        add(new BottomPanel(app), BorderLayout.SOUTH);

        loadUsersIntoTable();
        hookEvents();
    }

    private JPanel makeMiddle() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setOpaque(false);

        //dont let people edit cells directly
        model = new DefaultTableModel(new Object[]{"ID", "Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tbl = new JTable(model);
        JScrollPane sp = new JScrollPane(tbl);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Manage Users"));

        userTxt = new JTextField();
        passTxt = new JPasswordField();

        //these are all the current allowed roles. in a drop down menu
        //case sensitive i think.
        roleDrop = new JComboBox<>(new String[]{
                "Administrator",
                "Director of Operations/Manager",
                "Senior accountant",
                "Accountant",
                "Pharmacist"
        });

        addBtn = new JButton("add User");
        updBtn = new JButton("update Role");
        delBtn = new JButton("delete User");
        clrBtn = new JButton("clear");

        form.add(new JLabel("username:"));
        form.add(userTxt);

        form.add(new JLabel("password:"));
        form.add(passTxt);

        form.add(new JLabel("role:"));
        form.add(roleDrop);

        form.add(addBtn);
        form.add(updBtn);

        form.add(delBtn);
        form.add(clrBtn);

        p.add(sp, BorderLayout.CENTER);
        p.add(form, BorderLayout.SOUTH);

        return p;
    }

    private void hookEvents() {

        //for when you click a row, fill the form.
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            int row = tbl.getSelectedRow();

            if (row != -1) {
                String uname = model.getValueAt(row, 1).toString();
                String r = model.getValueAt(row, 2).toString();

                userTxt.setText(uname);

                //dont let username get changed if updating/deleting
                userTxt.setEditable(false);

                //not updating password here
                passTxt.setText("");

                roleDrop.setSelectedItem(r);
            }
        });

        addBtn.addActionListener(e -> {
            String uname = userTxt.getText().trim();
            String pw = new String(passTxt.getPassword()).trim();
            String r = roleDrop.getSelectedItem().toString();

            //basic validation only
            if (uname.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "username and password required.");
                return;
            }

            boolean ok = usersDao.createUser(uname, pw, r);

            if (ok) {
                JOptionPane.showMessageDialog(this, "user created.");
                resetForm();
                loadUsersIntoTable();
            } else {
                JOptionPane.showMessageDialog(this, "could not create user.");
            }
        });

        updBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "select a user first.");
                return;
            }

            String uname = userTxt.getText().trim();
            String pickedRole = roleDrop.getSelectedItem().toString();

            //prototype rule.. only admin
            if (uname.equals(app.getCurrentUser().getUsername())) {
                JOptionPane.showMessageDialog(this, "admin account cannot be changed ");
                return;
            }

            boolean ok = usersDao.updateUserRole(uname, pickedRole);

            if (ok) {
                JOptionPane.showMessageDialog(this, "role updated.");
                resetForm();
                loadUsersIntoTable();
            } else {
                JOptionPane.showMessageDialog(this, "could not update role");
            }
        });

        delBtn.addActionListener(e -> {
            int row = tbl.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "select user first");
                return;
            }

            String uname = userTxt.getText().trim();

            //dont let admin break their own account here
            if (uname.equals(app.getCurrentUser().getUsername())) {
                JOptionPane.showMessageDialog(this, "admin account cannot be deleted");
                return;
            }

            //contfirmation box
            int yesNo = JOptionPane.showConfirmDialog(
                    this,
                    "delete user: " + uname + "?",
                    "confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (yesNo == JOptionPane.YES_OPTION) {
                boolean ok = usersDao.deleteUser(uname);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "user deleted.");
                    resetForm();
                    loadUsersIntoTable();
                } else {
                    JOptionPane.showMessageDialog(this, "could not delete user.");
                }
            }
        });

        clrBtn.addActionListener(e -> resetForm());
    }

    private void loadUsersIntoTable() {
        //wipe old rows first
        model.setRowCount(0);
        List<User> allUsers = usersDao.getAllUsers();

        //dao gives back a list
        for (User u : allUsers) {
            model.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getRole()
            });
        }
    }

    private void resetForm() {
        //clear current selection and fields
        tbl.clearSelection();
        userTxt.setText("");
        passTxt.setText("");
        roleDrop.setSelectedIndex(0);

        //back to add mode
        userTxt.setEditable(true);
    }
}