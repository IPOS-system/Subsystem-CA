package service;

import dao.TemplateDAO;
import domain.Template;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.List;
import java.util.Map;


public class TemplateService {

    private final TemplateDAO dao = new TemplateDAO();


    public List<Template> listTemplates() {
        return dao.findAll();
    }

    public Template getTemplate(int id) {
        return dao.findById(id);
    }


    public boolean saveTemplate(Template tmpl) {
        if (tmpl.getName() == null || tmpl.getName().trim().isEmpty())
            throw new IllegalArgumentException("Template name cannot be empty");
        if (tmpl.getType() == null || tmpl.getType().trim().isEmpty())
            throw new IllegalArgumentException("Template type cannot be empty");

        if (tmpl.getId() == null) {
            return dao.insert(tmpl);
        } else {
            return dao.update(tmpl);
        }
    }

    public boolean deleteTemplate(int id) {
        return dao.delete(id);
    }



    public byte[] renderTemplate(int templateId, Map<String, String> placeholders) throws IOException {
        Template tmpl = getTemplate(templateId);
        if (tmpl == null) {
            throw new IllegalArgumentException("Template not found");
        }

        if (tmpl.getBinaryContent() == null && (tmpl.getFilePath() == null || tmpl.getFilePath().isBlank())) {
            String rendered = tmpl.getContent() == null ? "" : tmpl.getContent();
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                rendered = rendered.replace("${" + e.getKey() + "}", e.getValue());
            }
            return rendered.getBytes("UTF-8");
        }

        InputStream docStream;
        if (tmpl.getBinaryContent() != null) {
            docStream = new ByteArrayInputStream(tmpl.getBinaryContent());
        } else {
            // fallback: read directly from the file the user selected (may be unsaved yet)
            File f = new File(tmpl.getFilePath());
            if (!f.exists()) {
                throw new FileNotFoundException("Template file not found: " + tmpl.getFilePath());
            }
            docStream = new FileInputStream(f);
        }

        XWPFDocument doc = new XWPFDocument(docStream);

        // replace placeholders in all paragraphs
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraph(p, placeholders);
        }
        // replace placeholders inside tables (if any)
        for (XWPFTable tbl : doc.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceInParagraph(p, placeholders);
                    }
                }
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.write(out);
            return out.toByteArray();
        } finally {
            doc.close();
            docStream.close();
        }
    }


    private void replaceInParagraph(XWPFParagraph p, Map<String, String> placeholders) {
        for (XWPFRun run : p.getRuns()) {
            String txt = run.getText(0);
            if (txt != null) {
                for (Map.Entry<String, String> e : placeholders.entrySet()) {
                    txt = txt.replace("${" + e.getKey() + "}", e.getValue());
                }
                run.setText(txt, 0);
            }
        }
    }
}
