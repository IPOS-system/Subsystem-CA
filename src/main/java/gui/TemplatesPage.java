package gui;

import domain.Template;
import service.AppController;
import service.TemplateService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.awt.image.BufferedImage;

/**
 * TemplatesPage – clean UI that now:
 *
 *   • lets you **upload a .txt** file (its content goes straight into the editor).<br>
 *   • lets you add **any number of PNG logos** (they are appended at the bottom of the document).<br>
 *   • shows **preview in‑place** (a scrollable image of the first PDF page).<br>
 *   • removes the CSV table maker and the Word‑doc checkbox.<br>
 *
 * All GUI → Service → DAO → DB flow is preserved.
 */
public class TemplatesPage extends JPanel {

    private final AppController appController;
    private final TemplateService templateService;

    private JList<Template> templateJList;
    private DefaultListModel<Template> listModel;
    private List<Template> allTemplates;


    // ---------------- filter UI ----------------
    private JComboBox<String> cmbFilterType;
    private JTextField txtSearch;

    // ---------------- editor UI ----------------
    private JTextField txtName;
    private JComboBox<String> cmbType;
    private JTextArea txtContent;
    private JScrollPane txtScroll;

    // ---------------- logo UI (multiple) ----------------
    private DefaultListModel<String> logoListModel;
    private JList<String> logoList;
    private JButton btnAddLogo, btnRemoveLogo;

    // ---------------- buttons ----------------
    private JButton btnNew, btnEdit, btnSave, btnDelete, btnPreview, btnExport, btnUploadTxt;

    // ---------------- preview panel (in‑place) ----------------
    private JPanel previewPanel;               // holds the preview image + back button
    private JLabel previewLabel;                // image of the PDF page
    private JButton btnBackToEdit;
    private JPanel cardPanel;          // holds the "EDITOR" and "PREVIEW" cards


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
       BUILD THE CENTER PANEL (list + editor + preview)
       ------------------------------------------------------------ */
    private JPanel buildCenterPanel() {
        JPanel centre = new JPanel(new BorderLayout(10, 10));
        centre.setBorder(new EmptyBorder(10, 10, 10, 10));
        centre.setOpaque(false);

        /* ------------------ LEFT (list + filter) ------------------ */
        JPanel leftPanel = new JPanel(new BorderLayout(5,5));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        cmbFilterType = new JComboBox<>(new String[]{"ALL","REMINDER","RECEIPT","INVOICE","OTHER"});
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

        /* ------------------ RIGHT (editor + preview) ------------------ */
        JPanel rightPanel = new JPanel(new CardLayout());   // two cards: editor / preview

        /* ---------- EDITOR CARD ---------- */
        JPanel editorCard = new JPanel(new BorderLayout(5,5));

        // ---- top fields (name + type) ----
        JPanel topFields = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0 – template name
        gc.gridx = 0; gc.gridy = 0;
        topFields.add(new JLabel("Template name:"), gc);
        txtName = new JTextField(30);
        gc.gridx = 1;
        topFields.add(txtName, gc);

        // Row 1 – type selector
        gc.gridx = 0; gc.gridy = 1;
        topFields.add(new JLabel("Type:"), gc);
        cmbType = new JComboBox<>(new String[]{"REMINDER","RECEIPT","INVOICE","OTHER"});
        gc.gridx = 1;
        topFields.add(cmbType, gc);

        editorCard.add(topFields, BorderLayout.NORTH);

        // ---- plain‑text editor (center) ----
        txtContent = new JTextArea(20,40);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        // Approximate A4 width (595 px) – height scrolls
        txtContent.setPreferredSize(new Dimension(595,842));
        txtScroll = new JScrollPane(txtContent);
        editorCard.add(txtScroll, BorderLayout.CENTER);

        // ---- logo list + add/remove buttons (south left) ----
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

        logoPanel.add(new JLabel("Logos (will appear at the bottom)"), BorderLayout.NORTH);
        logoPanel.add(logoScroll, BorderLayout.CENTER);
        logoPanel.add(logoBtnPanel, BorderLayout.SOUTH);

        // ---- bottom buttons (row) ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNew      = new JButton("New");
        btnEdit     = new JButton("Edit");
        btnSave     = new JButton("Save");
        btnDelete   = new JButton("Delete");
        btnPreview  = new JButton("Preview");
        btnExport   = new JButton("Export…");
        btnUploadTxt = new JButton("Upload .txt");
        btnPanel.add(btnUploadTxt);
        btnPanel.add(btnNew);
        btnPanel.add(btnEdit);
        btnPanel.add(btnSave);
        btnPanel.add(btnDelete);
        btnPanel.add(btnPreview);
        btnPanel.add(btnExport);

        // Assemble south part
        JPanel southPanel = new JPanel(new BorderLayout(5,5));
        southPanel.add(logoPanel, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);
        editorCard.add(southPanel, BorderLayout.SOUTH);

        /* ---------- PREVIEW CARD ---------- */
        previewPanel = new JPanel(new BorderLayout(5,5));
        previewLabel = new JLabel();                     // will hold an ImageIcon
        JScrollPane previewScroll = new JScrollPane(previewLabel);
        btnBackToEdit = new JButton("Back to edit");
        previewPanel.add(previewScroll, BorderLayout.CENTER);
        previewPanel.add(btnBackToEdit, BorderLayout.SOUTH);

        /* ---------- CARD LAYOUT ---------- */
        cardPanel = new JPanel(new CardLayout());   // <‑‑ store it in the field
        cardPanel.add(editorCard, "EDITOR");
        cardPanel.add(previewPanel, "PREVIEW");

        /* ---------- MAIN SPLIT ---------- */
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, cardPanel);
        mainSplit.setResizeWeight(0.25);
        centre.add(mainSplit, BorderLayout.CENTER);

        return centre;
    }

    /* ------------------------------------------------------------
       LOAD & FILTER
       ------------------------------------------------------------ */
    private void loadTemplatesFromDb() {
        allTemplates = templateService.listTemplates();
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
        });

        cmbFilterType.addActionListener(e -> applyFilters());

        txtSearch.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override public void update() { applyFilters(); }
        });

        // ---------- Upload .txt ----------
        btnUploadTxt.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
            int rc = chooser.showOpenDialog(this);
            if (rc == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try {
                    String content = Files.readString(f.toPath());
                    txtContent.setText(content);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Could not read the text file.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ---------- Logo add / remove ----------
        btnAddLogo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));
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

        // ---------- NEW ----------
        btnNew.addActionListener(e -> {
            templateJList.clearSelection();
            clearEditor();
            setEditMode(true);
        });

        // ---------- EDIT ----------
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

        // ---------- SAVE ----------
        btnSave.addActionListener(e -> saveCurrentTemplate());

        // ---------- DELETE ----------
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

        // ---------- PREVIEW ----------
        btnPreview.addActionListener(e -> {
            Template tmpl = buildTemplateFromEditor();   // builds from UI fields
            if (tmpl == null) return;                 // error already shown

            Map<String,String> demoPlaceholders = Map.of(
                    "CUSTOMER_NAME", "John Doe",
                    "DUE_DATE",      java.time.LocalDate.now().plusDays(7).toString(),
                    "TOTAL",         "£123.45"
            );

            try {
                byte[] pdfBytes = templateService.renderAsPdf(tmpl, demoPlaceholders);
                // Convert the first PDF page to an image
                BufferedImage img;
                try (PDDocument pdf = PDDocument.load(pdfBytes)) {
                    PDFRenderer renderer = new PDFRenderer(pdf);
                    img = renderer.renderImageWithDPI(0, 150);   // 150 DPI → nice size
                }

                // Show the image in the preview panel
                previewLabel.setIcon(new ImageIcon(img));

                // ---- SWITCH TO THE PREVIEW CARD ----
                CardLayout cl = (CardLayout) cardPanel.getLayout();   // <‑‑ **fixed**
                cl.show(cardPanel, "PREVIEW");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Could not render preview: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        // ---------- BACK FROM PREVIEW ----------
        btnBackToEdit.addActionListener(e -> {
            CardLayout cl = (CardLayout) cardPanel.getLayout();   // <‑‑ **fixed**
            cl.show(cardPanel, "EDITOR");
        });


        // ---------- EXPORT ----------
        btnExport.addActionListener(e -> {
            Template tmpl = buildTemplateFromEditor();
            if (tmpl == null) return;

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export template");
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Word Document (*.docx)", "docx"));
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF Document (*.pdf)", "pdf"));

            int rc = chooser.showSaveDialog(this);
            if (rc != JFileChooser.APPROVE_OPTION) return;
            File target = chooser.getSelectedFile();
            String ext = ((FileNameExtensionFilter) chooser.getFileFilter())
                    .getExtensions()[0];
            if (!target.getName().toLowerCase().endsWith("." + ext))
                target = new File(target.getParentFile(), target.getName() + "." + ext);

            Map<String,String> demoPlaceholders = Map.of(
                    "CUSTOMER_NAME", "John Doe",
                    "DUE_DATE",      LocalDate.now().plusDays(7).toString(),
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
       Helper – populate / clear / edit‑mode toggle / build Template
       ------------------------------------------------------------ */
    private void populateEditor(Template t) {
        if (t == null) {
            clearEditor();
            return;
        }
        txtName.setText(t.getName());
        cmbType.setSelectedItem(t.getType());
        txtContent.setText(t.getContent() == null ? "" : t.getContent());

        // logos
        logoListModel.clear();
        if (t.getLogoPaths() != null)
            t.getLogoPaths().forEach(logoListModel::addElement);
    }

    private void clearEditor() {
        txtName.setText("");
        cmbType.setSelectedIndex(0);
        txtContent.setText("");
        logoListModel.clear();
        setEditMode(false);
    }

    private void setEditMode(boolean enabled) {
        txtName.setEnabled(enabled);
        cmbType.setEnabled(enabled);
        txtContent.setEnabled(enabled);
        btnAddLogo.setEnabled(enabled);
        btnRemoveLogo.setEnabled(enabled);
        btnSave.setEnabled(enabled);
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
        tmpl.setContent(txtContent.getText());

        // logos (ordered)
        List<String> logos = Collections.list(logoListModel.elements());
        tmpl.setLogoPaths(logos);

        // preserve id if editing existing row
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

                // re‑select the saved template in the list
                if (tmpl.getId() != null) {
                    for (int i = 0; i < listModel.size(); i++) {
                        if (listModel.get(i).getId().equals(tmpl.getId())) {
                            templateJList.setSelectedIndex(i);
                            break;
                        }
                    }
                }
                setEditMode(false);
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
     * Build a {@link Template} instance that mirrors the current UI fields.
     * Used for preview and export without persisting first.
     */
    private Template buildTemplateFromEditor() {
        Template tmpl = new Template();

        tmpl.setName(txtName.getText().trim());
        tmpl.setType((String) cmbType.getSelectedItem());
        tmpl.setContent(txtContent.getText());

        List<String> logos = Collections.list(logoListModel.elements());
        tmpl.setLogoPaths(logos);

        // keep id if editing an existing template
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

    //tester
    public static void show(AppController controller) {
        JFrame f = new JFrame("IPOS‑CA – Templates");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setContentPane(new TemplatesPage(controller));
        f.setSize(1300, 900);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
