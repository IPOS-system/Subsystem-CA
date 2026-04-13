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
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.awt.image.BufferedImage;

/**
 * TemplatesPage – manager can
 *   • list, filter, edit, save, delete templates
 *   • add **any number of PNG logos** (logos are stored per‑template)
 *   • type {@code ${LOGO}} in the editor – each occurrence uses the next logo
 *   • type {@code ${TABLE}} to insert a table (CSV data defined in the "Table Data" area)
 *   • edit a selected template (Edit button)
 *   • preview **inside the app** – width limited to A4 (≈ 595 px)
 *   • export to **Word (.docx)** or **PDF**
 *
 * All heavy‑lifting is delegated to {@link TemplateService}.
 */
public class TemplatesPage extends JPanel {

    private final AppController appController;
    private final TemplateService templateService;

    private JList<Template> templateJList;
    private DefaultListModel<Template> listModel;
    private List<Template> allTemplates;

    // ---- filter UI ----
    private JComboBox<String> cmbFilterType;
    private JTextField txtSearch;

    // ---- editor fields ----
    private JTextField txtName;
    private JComboBox<String> cmbType;
    private JCheckBox chkWordDoc;
    private JTextField txtFilePath;
    private JButton btnBrowseFile;

    // ---- logo UI (multiple) ----
    private DefaultListModel<String> logoListModel;
    private JList<String> logoList;
    private JButton btnAddLogo, btnRemoveLogo;

    // ---- plain‑text area ----
    private JTextArea txtContent;
    private JScrollPane txtScroll;
    private JSplitPane verticalSplit;

    // ---- table data UI ----
    private JTextArea txtTableData;
    private JScrollPane tableScroll;

    // ---- action buttons ----
    private JButton btnNew, btnEdit, btnSave, btnDelete, btnPreview, btnExport;

    public TemplatesPage(AppController appController) {
        this.appController = appController;
        this.templateService = appController.getTemplateService();

        setLayout(new BorderLayout());

        add(new HeaderPanel(appController), BorderLayout.NORTH);
        add(new BottomPanel(appController), BorderLayout.SOUTH);
        add(buildCenterPanel(), BorderLayout.CENTER);

        loadTemplatesFromDb();
        hookEvents();
    }

    /* ------------------------------------------------------------
       BUILD THE CENTER PANEL (list + editor)
       ------------------------------------------------------------ */
    private JPanel buildCenterPanel() {
        JPanel centre = new JPanel(new BorderLayout(10, 10));
        centre.setBorder(new EmptyBorder(10, 10, 10, 10));
        centre.setOpaque(false);

        /* ------------------ LEFT (list + filter) ------------------ */
        JPanel leftPanel = new JPanel(new BorderLayout(5,5));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        cmbFilterType = new JComboBox<>(new String[]{"ALL","REMINDER","RECEIPT","INVOICE"});
        txtSearch = new JTextField(12);
        filterBar.add(new JLabel("Type:"));
        filterBar.add(cmbFilterType);
        filterBar.add(new JLabel("Search:"));
        filterBar.add(txtSearch);
        leftPanel.add(filterBar, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        templateJList = new JList<>(listModel);
        templateJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(templateJList);
        listScroll.setPreferredSize(new Dimension(200,0));
        leftPanel.add(listScroll, BorderLayout.CENTER);

        /* ------------------ RIGHT (editor) ------------------ */
        JPanel editor = new JPanel(new BorderLayout(5,5));

        // ----- fields panel (grid bag) -----
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

        // Row 3 – .docx file chooser
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        JPanel filePanel = new JPanel(new BorderLayout(5,0));
        txtFilePath = new JTextField(20);
        txtFilePath.setEditable(false);
        btnBrowseFile = new JButton("Browse…");
        filePanel.add(txtFilePath, BorderLayout.CENTER);
        filePanel.add(btnBrowseFile, BorderLayout.EAST);
        fieldsPanel.add(filePanel, gc);
        gc.gridwidth = 1;

        // Row 4 – Logos list + add / remove buttons
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        JPanel logoPanel = new JPanel(new BorderLayout(5,0));
        logoListModel = new DefaultListModel<>();
        logoList = new JList<>(logoListModel);
        logoList.setVisibleRowCount(3);
        JScrollPane logoScroll = new JScrollPane(logoList);
        logoScroll.setPreferredSize(new Dimension(250,80));

        JPanel logoBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        btnAddLogo = new JButton("Add Logo");
        btnRemoveLogo = new JButton("Remove Selected");
        logoBtnPanel.add(btnAddLogo);
        logoBtnPanel.add(btnRemoveLogo);

        logoPanel.add(logoScroll, BorderLayout.CENTER);
        logoPanel.add(logoBtnPanel, BorderLayout.SOUTH);
        fieldsPanel.add(logoPanel, gc);
        gc.gridwidth = 1; // reset

        // Row 5 – Table data (optional CSV)
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        JPanel tablePanel = new JPanel(new BorderLayout(5,0));
        txtTableData = new JTextArea(5,30);
        txtTableData.setLineWrap(true);
        txtTableData.setWrapStyleWord(true);
        tableScroll = new JScrollPane(txtTableData);
        tablePanel.add(new JLabel("Table Data (CSV – one row per line, commas separate columns):"), BorderLayout.NORTH);
        tablePanel.add(tableScroll, BorderLayout.CENTER);
        fieldsPanel.add(tablePanel, gc);
        gc.gridwidth = 1; // reset

        // ----- plain‑text editor (vertical split) -----
        txtContent = new JTextArea(15,40);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        // Approximate A4 width (595 px) – height scrolls
        txtContent.setPreferredSize(new Dimension(595,842));
        txtScroll = new JScrollPane(txtContent);
        txtScroll.setMinimumSize(new Dimension(200,100));

        verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, fieldsPanel, txtScroll);
        verticalSplit.setResizeWeight(0.6);
        verticalSplit.setOneTouchExpandable(true);
        editor.add(verticalSplit, BorderLayout.CENTER);

        // ----- action button bar -----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNew      = new JButton("New");
        btnEdit     = new JButton("Edit");
        btnSave     = new JButton("Save");
        btnDelete   = new JButton("Delete");
        btnPreview  = new JButton("Preview");
        btnExport   = new JButton("Export…");
        btnPanel.add(btnNew);
        btnPanel.add(btnEdit);
        btnPanel.add(btnSave);
        btnPanel.add(btnDelete);
        btnPanel.add(btnPreview);
        btnPanel.add(btnExport);
        editor.add(btnPanel, BorderLayout.SOUTH);

        // Main split (list vs editor)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, editor);
        mainSplit.setResizeWeight(0.25);
        centre.add(mainSplit, BorderLayout.CENTER);
        return centre;
    }

    /* ------------------------------------------------------------
       LOAD & FILTER
       ------------------------------------------------------------ */
    private void loadTemplatesFromDb() {
        allTemplates = templateService.listTemplates();   // list view (no BLOB)
        applyFilters();
    }

    private void applyFilters() {
        String selectedType = (String) cmbFilterType.getSelectedItem(); // may be "ALL"
        String searchText   = txtSearch.getText().trim().toLowerCase();

        List<Template> filtered = allTemplates.stream()
                .filter(t -> {
                    if (!"ALL".equalsIgnoreCase(selectedType) && !selectedType.equalsIgnoreCase(t.getType()))
                        return false;
                    return t.getName().toLowerCase().contains(searchText);
                })
                .collect(Collectors.toList());

        listModel.clear();
        filtered.forEach(listModel::addElement);
    }

    /* ------------------------------------------------------------
       EVENT HOOKS
       ------------------------------------------------------------ */
    private void hookEvents() {
        templateJList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Template sel = templateJList.getSelectedValue();
            populateEditor(sel);
            setEditMode(false);            // disable editing until user clicks Edit
        });

        cmbFilterType.addActionListener(e -> applyFilters());

        txtSearch.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override public void update() { applyFilters(); }
        });

        chkWordDoc.addActionListener(e -> {
            boolean isDoc = chkWordDoc.isSelected();
            txtContent.setEnabled(!isDoc);
            btnBrowseFile.setEnabled(isDoc);
        });

        btnBrowseFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("DOCX files", "docx"));
            int rc = chooser.showOpenDialog(this);
            if (rc == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                txtFilePath.setText(f.getAbsolutePath());
                chkWordDoc.setSelected(true);
            }
        });

        // ----- LOGO LIST -----
        btnAddLogo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG images", "png"));
            int rc = chooser.showOpenDialog(this);
            if (rc == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                logoListModel.addElement(f.getAbsolutePath());
            }
        });

        btnRemoveLogo.addActionListener(e -> {
            int idx = logoList.getSelectedIndex();
            if (idx >= 0) logoListModel.remove(idx);
        });

        // ----- CRUD -----
        btnNew.addActionListener(e -> {
            templateJList.clearSelection();
            clearEditor();
            setEditMode(true);
        });

        btnEdit.addActionListener(e -> {
            Template sel = templateJList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(this,
                        "Select a template to edit.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            setEditMode(true);
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
                loadTemplatesFromDb();
                clearEditor();
            }
        });

        // ------------------------------------------------------------
// PREVIEW BUTTON
// ------------------------------------------------------------
        btnPreview.addActionListener(e -> {
            // Build a temporary Template that mirrors the editor – **do NOT clear the selection**
            Template tmpl = buildTemplateFromEditor();
            if (tmpl == null) return;   // error already shown

            Map<String,String> demoPlaceholders = Map.of(
                    "CUSTOMER_NAME", "John Doe",
                    "DUE_DATE",      java.time.LocalDate.now().plusDays(7).toString(),
                    "TOTAL",         "£123.45"
            );

            try {
                // PDF preview – we always render a PDF (logos, tables, etc.)
                byte[] pdfBytes = templateService.renderAsPdf(tmpl, demoPlaceholders);
                showPdfPreview(pdfBytes, "Template preview (PDF)");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Could not render preview: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ----- EXPORT -----
        btnExport.addActionListener(e -> {
            Template tmpl = buildTemplateFromEditor();
            if (tmpl == null) return;

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export template");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Word Document (*.docx)", "docx"));
            chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));

            int rc = chooser.showSaveDialog(this);
            if (rc != JFileChooser.APPROVE_OPTION) return;
            File target = chooser.getSelectedFile();
            String ext = ((javax.swing.filechooser.FileNameExtensionFilter) chooser.getFileFilter())
                    .getExtensions()[0];
            if (!target.getName().toLowerCase().endsWith("." + ext))
                target = new File(target.getParentFile(), target.getName() + "." + ext);

            Map<String,String> demoPlaceholders = Map.of(
                    "CUSTOMER_NAME", "John Doe",
                    "DUE_DATE",      java.time.LocalDate.now().plusDays(7).toString(),
                    "TOTAL",         "£123.45"
            );

            try {
                byte[] data;
                if ("pdf".equalsIgnoreCase(ext)) {
                    data = templateService.renderAsPdf(tmpl, demoPlaceholders);
                } else {
                    data = templateService.renderAsDocx(tmpl, demoPlaceholders);
                }
                Files.write(target.toPath(), data);
                JOptionPane.showMessageDialog(this,
                        "Exported to " + target.getAbsolutePath(),
                        "Export successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Export failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /* ------------------------------------------------------------
       Helper – show a PDF preview (first page) inside a dialog.
       ------------------------------------------------------------ */
    private void showPdfPreview(byte[] pdfBytes, String title) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage img = renderer.renderImageWithDPI(0, 150); // 150 DPI → readable size
            ImageIcon icon = new ImageIcon(img);
            JLabel label = new JLabel(icon);
            JScrollPane sp = new JScrollPane(label);
            sp.setPreferredSize(new Dimension(595, 842)); // A4 size in pixels at 150 DPI ≈ 595×842
            JOptionPane.showMessageDialog(this, sp, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /* ------------------------------------------------------------
       Populate / clear / edit‑mode toggle / build template from UI
       ------------------------------------------------------------ */
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

        // logos
        logoListModel.clear();
        if (t.getLogoPaths() != null)
            t.getLogoPaths().forEach(logoListModel::addElement);

        // table data
        txtTableData.setText(t.getTableData() == null ? "" : t.getTableData());
    }

    private void clearEditor() {
        txtName.setText("");
        cmbType.setSelectedIndex(0);
        chkWordDoc.setSelected(false);
        txtContent.setText("");
        txtFilePath.setText("");
        txtContent.setEnabled(true);
        btnBrowseFile.setEnabled(false);
        logoListModel.clear();
        txtTableData.setText("");
    }

    /** Enable editing of all fields (except the list). */
    private void setEditMode(boolean enabled) {
        txtName.setEnabled(enabled);
        cmbType.setEnabled(enabled);
        chkWordDoc.setEnabled(enabled);
        txtContent.setEnabled(enabled && !chkWordDoc.isSelected());
        btnBrowseFile.setEnabled(enabled && chkWordDoc.isSelected());
        btnAddLogo.setEnabled(enabled);
        btnRemoveLogo.setEnabled(enabled);
        txtTableData.setEnabled(enabled);
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

        // ---- .docx handling ----
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
                tmpl.setBinaryContent(Files.readAllBytes(f.toPath()));
                tmpl.setContent(null);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Could not read the selected file.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // plain‑text template (logos & tables are allowed)
            tmpl.setContent(txtContent.getText());
            tmpl.setBinaryContent(null);
        }

        // ---- logos (ordered) ----
        List<String> logos = Collections.list(logoListModel.elements());
        tmpl.setLogoPaths(logos);

        // ---- table data (CSV) ----
        String csv = txtTableData.getText().trim();
        tmpl.setTableData(csv.isEmpty() ? null : csv);

        // ---- preserve id if editing an existing row ----
        Template selected = templateJList.getSelectedValue();
        if (selected != null && selected.getId() != null) {
            tmpl.setId(selected.getId());
        }

        try {
            boolean ok = templateService.saveTemplate(tmpl);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Template saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
                // Refresh the list but **keep the current selection** (so the editor stays populated)
                loadTemplatesFromDb();
                // re‑select the saved row
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


    /**
     * Build a {@link Template} instance that mirrors the current UI.
     * Used for preview and export without persisting first.
     */
    private Template buildTemplateFromEditor() {
        Template tmpl = new Template();

        tmpl.setName(txtName.getText().trim());
        tmpl.setType((String) cmbType.getSelectedItem());

        // logos
        List<String> logos = Collections.list(logoListModel.elements());
        tmpl.setLogoPaths(logos);

        // table data
        String csv = txtTableData.getText().trim();
        tmpl.setTableData(csv.isEmpty() ? null : csv);

        if (chkWordDoc.isSelected()) {
            String path = txtFilePath.getText().trim();
            if (!path.isEmpty()) {
                try {
                    tmpl.setBinaryContent(Files.readAllBytes(Paths.get(path)));
                    tmpl.setFilePath(null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Could not read .docx file.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
        } else {
            tmpl.setContent(txtContent.getText());
            tmpl.setBinaryContent(null);
            tmpl.setFilePath(null);
        }

        // keep id if editing an existing template (useful for logo fallback logic)
        Template selected = templateJList.getSelectedValue();
        if (selected != null && selected.getId() != null) {
            tmpl.setId(selected.getId());
        }
        return tmpl;
    }

    /* -----------------------------------------------------------------
       Helper to listen to any change in the search field
       ----------------------------------------------------------------- */
    private abstract static class DocumentAdapter implements javax.swing.event.DocumentListener {
        public abstract void update();
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }

    // -----------------------------------------------------------------
    // Simple static tester – launch the page in its own window
    // -----------------------------------------------------------------
    public static void show(AppController controller) {
        JFrame f = new JFrame("IPOS‑CA – Templates");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setContentPane(new TemplatesPage(controller));
        f.setSize(1300, 900);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
