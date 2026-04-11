package gui;

import domain.Template;
import service.AppController;
import service.TemplateService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TemplatesPage – manager can
 *   • list all templates,
 *   • filter by **type** (REMINDER/RECEIPT/INVOICE) and by **name**,
 *   • edit plain‑text or .docx templates,
 *   • resize the plain‑text editor by dragging,
 *   • preview the rendered document (docx opens in the OS; plain‑text shows in a dialog).
 *
 * All business logic stays in {@link TemplateService}; this class only handles UI events.
 */
public class TemplatesPage extends JPanel {

    private final AppController appController;
    private final TemplateService templateService = new TemplateService();


    private JList<Template> templateJList;
    // left side list
    private DefaultListModel<Template> listModel;
    // backing model for JList
    private List<Template> allTemplates;
    // full list as returned from DB

    // Filter UI (above the list)
    private JComboBox<String> cmbFilterType;
    // ALL / REMINDER / RECEIPT / INVOICE
    private JTextField txtSearch;
    // name search

    // Editor fields (right side)
    private JTextField txtName;
    private JComboBox<String> cmbType;                  // template type selector
    private JCheckBox chkWordDoc;                       // “this is a .docx?” flag
    private JTextField txtFilePath;                     // shows chosen .docx path (read‑only)
    private JButton btnBrowseFile;                      // opens file chooser

    // Plain‑text editor – placed in a **vertical split pane** so the user can drag it
    private JTextArea txtContent;                       // plain‑text body
    private JScrollPane txtScroll;                      // scroll pane that contains txtContent
    private JSplitPane verticalSplit;                   // splitter between fields and txtArea

    // Action buttons
    private JButton btnNew, btnSave, btnDelete, btnPreview;


    public TemplatesPage(AppController appController) {
        this.appController = appController;

        setLayout(new BorderLayout());

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
        add(buildCenterPanel(), BorderLayout.CENTER);

        loadTemplatesFromDb();
        hookEvents();
    }


    private JPanel buildCenterPanel() {
        JPanel centre = new JPanel(new BorderLayout(10, 10));
        centre.setBorder(new EmptyBorder(10, 10, 10, 10));
        centre.setOpaque(false);

        JPanel leftPanel = new JPanel(new BorderLayout(5,5));

        // Filter bar (type combo + search field)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        cmbFilterType = new JComboBox<>(new String[]{"ALL","REMINDER","RECEIPT","INVOICE"});
        txtSearch   = new JTextField(12);
        filterBar.add(new JLabel("Type:"));
        filterBar.add(cmbFilterType);
        filterBar.add(new JLabel("Search:"));
        filterBar.add(txtSearch);
        leftPanel.add(filterBar, BorderLayout.NORTH);

        // JList of templates
        listModel = new DefaultListModel<>();
        templateJList = new JList<>(listModel);
        templateJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(templateJList);
        listScroll.setPreferredSize(new Dimension(200,0));
        leftPanel.add(listScroll, BorderLayout.CENTER);

        JPanel editor = new JPanel(new BorderLayout(5,5));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0 – Template name
        gc.gridx = 0; gc.gridy = 0;
        fieldsPanel.add(new JLabel("Template name:"), gc);
        txtName = new JTextField(30);
        gc.gridx = 1;
        fieldsPanel.add(txtName, gc);

        // Row 1 – Type selector
        gc.gridx = 0; gc.gridy = 1;
        fieldsPanel.add(new JLabel("Type:"), gc);
        cmbType = new JComboBox<>(new String[]{"REMINDER","RECEIPT","INVOICE"});
        gc.gridx = 1;
        fieldsPanel.add(cmbType, gc);

        // Row 2 – Word‑doc flag
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        chkWordDoc = new JCheckBox("Template is a Word document (.docx)");
        fieldsPanel.add(chkWordDoc, gc);
        gc.gridwidth = 1; // reset

        // Row 3 – File chooser (only enabled when chkWordDoc is true)
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        JPanel filePanel = new JPanel(new BorderLayout(5,0));
        txtFilePath = new JTextField(20);
        txtFilePath.setEditable(false);
        btnBrowseFile = new JButton("Browse…");
        filePanel.add(txtFilePath, BorderLayout.CENTER);
        filePanel.add(btnBrowseFile, BorderLayout.EAST);
        fieldsPanel.add(filePanel, gc);
        gc.gridwidth = 1;

        txtContent = new JTextArea(15,40);
        // a decent default height (15 rows)
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtScroll = new JScrollPane(txtContent);
        txtScroll.setMinimumSize(new Dimension(200,100));

        verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, fieldsPanel, txtScroll);
        verticalSplit.setResizeWeight(0.6);   // 60% of the height goes to the fields, 40% to the text area
        verticalSplit.setOneTouchExpandable(true); // little arrows on the divider
        editor.add(verticalSplit, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNew     = new JButton("New");
        btnSave    = new JButton("Save");
        btnDelete  = new JButton("Delete");
        btnPreview = new JButton("Preview");
        btnPanel.add(btnNew);
        btnPanel.add(btnSave);
        btnPanel.add(btnDelete);
        btnPanel.add(btnPreview);
        editor.add(btnPanel, BorderLayout.SOUTH);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, editor);
        mainSplit.setResizeWeight(0.25); // 25% list, 75% editor
        centre.add(mainSplit, BorderLayout.CENTER);
        return centre;
    }


    private void loadTemplatesFromDb() {
        allTemplates = templateService.listTemplates(); // master list
        applyFilters();                               // fills listModel according to current filter values
    }


    private void applyFilters() {
        String selectedType = (String) cmbFilterType.getSelectedItem(); // may be "ALL"
        String searchText   = txtSearch.getText().trim().toLowerCase();

        List<Template> filtered = allTemplates.stream()
                .filter(t -> {
                    if (!"ALL".equalsIgnoreCase(selectedType) && !selectedType.equalsIgnoreCase(t.getType())) {
                        return false;
                    }
                    return t.getName().toLowerCase().contains(searchText);
                })
                .collect(Collectors.toList());

        listModel.clear();
        for (Template t : filtered) {
            listModel.addElement(t);
        }
    }


    private void hookEvents() {
        templateJList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Template sel = templateJList.getSelectedValue();
            populateEditor(sel);
        });

        cmbFilterType.addActionListener(e -> applyFilters());

        txtSearch.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override public void update() { applyFilters(); }
        });

        chkWordDoc.addActionListener(e -> {
            boolean isDoc = chkWordDoc.isSelected();
            txtContent.setEnabled(!isDoc);        // plain‑text disabled for .docx
            btnBrowseFile.setEnabled(isDoc);
        });

        btnBrowseFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("DOCX files", "docx"));
            int rc = chooser.showOpenDialog(this);
            if (rc == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                txtFilePath.setText(f.getAbsolutePath());
                // automatically tick the checkbox when a file is chosen
                chkWordDoc.setSelected(true);
            }
        });

        btnNew.addActionListener(e -> {
            templateJList.clearSelection();
            clearEditor();
        });

        btnSave.addActionListener(e -> saveCurrentTemplate());

        btnDelete.addActionListener(e -> {
            Template sel = templateJList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(this,
                        "Select a template to delete.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete template \"" + sel.getName() + "\"?",
                    "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                templateService.deleteTemplate(sel.getId());
                loadTemplatesFromDb();    // refresh master list
                clearEditor();
            }
        });

        btnPreview.addActionListener(e -> previewTemplate());
    }


    private void populateEditor(Template t) {
        if (t == null) {
            clearEditor();
            return;
        }
        txtName.setText(t.getName());
        cmbType.setSelectedItem(t.getType());

        boolean isDoc = t.getBinaryContent() != null;
        chkWordDoc.setSelected(isDoc);
        txtContent.setEnabled(!isDoc);
        btnBrowseFile.setEnabled(isDoc);
        txtFilePath.setText(isDoc ? "[binary .docx stored]" : "");

        txtContent.setText(t.getContent() == null ? "" : t.getContent());
    }

    private void clearEditor() {
        txtName.setText("");
        cmbType.setSelectedIndex(0);
        chkWordDoc.setSelected(false);
        txtContent.setText("");
        txtFilePath.setText("");
        txtContent.setEnabled(true);
        btnBrowseFile.setEnabled(false);
    }


    private void saveCurrentTemplate() {
        String name = txtName.getText().trim();
        String type = (String) cmbType.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Template name cannot be empty.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Template tmpl = new Template();
        tmpl.setName(name);
        tmpl.setType(type);

        // Worddoc handle WIP
        if (chkWordDoc.isSelected()) {
            String path = txtFilePath.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Select a .docx file for the template.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                File f = new File(path);
                byte[] data = Files.readAllBytes(f.toPath());
                tmpl.setBinaryContent(data);
                tmpl.setContent(null);      // plain‑text not used
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Could not read the selected file.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // Plain‑text template
            tmpl.setContent(txtContent.getText());
            tmpl.setBinaryContent(null);
        }

        // If a row is selected we are updating an existing template
        Template selected = templateJList.getSelectedValue();
        if (selected != null && selected.getId() != null) {
            tmpl.setId(selected.getId());
        }

        try {
            boolean ok = templateService.saveTemplate(tmpl);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Template saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
                loadTemplatesFromDb();
                // reload and apply current filters
                // re‑select the just‑saved template in the filtered list
                if (tmpl.getId() != null) {
                    for (int i = 0; i < listModel.size(); i++) {
                        if (listModel.get(i).getId().equals(tmpl.getId())) {
                            templateJList.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save template.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error while saving: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void previewTemplate() {
        Template sel = templateJList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a template to preview.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        //placeholder map
        Map<String,String> demoPlaceholders = Map.of(
                "CUSTOMER_NAME", "John Doe",
                "DUE_DATE",      java.time.LocalDate.now().plusDays(7).toString(),
                "TOTAL",         "£123.45"
        );

        try {
            byte[] rendered = templateService.renderTemplate(sel.getId(), demoPlaceholders);

            if (sel.getBinaryContent() != null) {
                File tmp = File.createTempFile("tmplPreview_", ".docx");
                Files.write(tmp.toPath(), rendered);
                Desktop.getDesktop().open(tmp);   // OS opens Word (or default .docx viewer)
                return;
            }

            String txt = new String(rendered, "UTF-8");
            JTextArea previewArea = new JTextArea(txt);
            previewArea.setEditable(false);
            previewArea.setLineWrap(true);
            previewArea.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(previewArea);
            sp.setPreferredSize(new Dimension(600,400));
            JOptionPane.showMessageDialog(this, sp,
                    "Template preview", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not render preview: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //helper
    private abstract static class DocumentAdapter implements javax.swing.event.DocumentListener {
        public abstract void update();
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }

    //testing
    public static void show(AppController controller) {
        JFrame f = new JFrame("IPOS‑CA – Templates");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setContentPane(new TemplatesPage(controller));
        f.setSize(1000, 720);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
