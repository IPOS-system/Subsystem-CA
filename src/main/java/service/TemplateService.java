package service;

import dao.TemplateDAO;
import dao.TemplateFileDAO;
import domain.MerchantInfo;
import domain.Template;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business‑logic layer for Templates.
 *
 * – CRUD still goes to the DB (TemplateDAO).<br>
 * – After a successful DB write we also persist the object to a
 *   file using {@link TemplateFileDAO}.<br>
 * – On application start we call {@link #syncTemplatesWithFilesystem()}
 *   to make sure DB and file‑system stay in sync.
 */
public class TemplateService {

    private final TemplateDAO dao = new TemplateDAO();
    private final TemplateFileDAO fileDao = new TemplateFileDAO();

    /* ------------------- FONT HELPERS ------------------- */
    private void forceArialFont(XWPFRun run) {
        run.setFontFamily("Arial");
    }

    /* ------------------- CRUD ------------------- */
    public List<Template> listTemplates() { return dao.findAll(); }

    public Template getTemplate(int id) { return dao.findById(id); }

    private static final Set<String> VALID_TYPES =
            Set.of("REMINDER","RECEIPT","INVOICE","OTHER");

    /** Saves to DB **and** to the file system. */
    public boolean saveTemplate(Template tmpl) {
        // ---- validation -------------------------------------------------
        if (!VALID_TYPES.contains(tmpl.getType()))
            throw new IllegalArgumentException("Invalid template type: " + tmpl.getType());
        if (tmpl.getName() == null || tmpl.getName().trim().isEmpty())
            throw new IllegalArgumentException("Template name cannot be empty");
        if (tmpl.getType() == null || tmpl.getType().trim().isEmpty())
            throw new IllegalArgumentException("Template type cannot be empty");

        // ---- DB write ----------------------------------------------------
        boolean dbOk = (tmpl.getId() == null) ? dao.insert(tmpl) : dao.update(tmpl);
        if (!dbOk) return false;              // DB failed → do NOT write file

        // ---- File write --------------------------------------------------
        // after dao.insert the id field is populated, so file naming works
        fileDao.save(tmpl);
        return true;
    }

    /** Deletes from DB **and** from the file system. */
    public boolean deleteTemplate(int id) {
        Template tmpl = dao.findById(id);          // we need the id for the file name
        boolean dbOk = dao.delete(id);
        if (dbOk && tmpl != null) {
            fileDao.delete(tmpl);
        }
        return dbOk;
    }

    /* -----------------------------------------------------------------
       PUBLIC RENDERERS (used by the GUI)
       ----------------------------------------------------------------- */
    /**
     * Render the template as a **DOCX** byte[].
     *
     * <p>
     *   • Plain‑text content is written line‑by‑line (preserving new‑lines).<br>
     *   • All logos are appended after the last paragraph (bottom of the doc).<br>
     *   • No placeholder processing – the template is pure text.
     * </p>
     */
    public byte[] renderAsDocx(Template tmpl, Map<String,String> placeholders) throws IOException {
        // Replace normal placeholders in the plain‑text body
        String body = tmpl.getContent() == null ? "" : tmpl.getContent();
        for (Map.Entry<String,String> e : placeholders.entrySet())
            body = body.replace("${" + e.getKey() + "}", e.getValue());

        XWPFDocument doc = new XWPFDocument();

        // Write the body line by line
        String[] lines = body.split("\n");
        for (String line : lines) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setText(line);
            forceArialFont(r);
        }

        // Append logos (if any) at the **end** of the document
        List<String> logos = tmpl.getLogoPaths();
        if (logos != null && !logos.isEmpty()) {
            for (String logoPath : logos) {
                addLogoParagraph(doc, logoPath);   // each logo gets its own paragraph
            }
        }

        // Return the DOCX bytes
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.write(out);
            return out.toByteArray();
        } finally {
            doc.close();
        }
    }

    /**
     * Render the template as a **PDF** (text + logos at the bottom).
     *
     * This method first builds the DOCX (so we reuse the same logic) and
     * then converts the extracted text + logos into a PDF.
     */
    public byte[] renderAsPdf(Template tmpl, Map<String,String> placeholders) throws IOException {
        // Build a DOCX (same as for Word export)
        byte[] docxBytes = renderAsDocx(tmpl, placeholders);

        // Load the DOCX with POI to extract the pure text (no tables any more)
        XWPFDocument doc;
        try (InputStream is = new ByteArrayInputStream(docxBytes)) {
            doc = new XWPFDocument(is);
        }

        // Create PDF
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        pdf.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(pdf, page);

        float margin = 50f;
        float cursorY = page.getMediaBox().getHeight() - margin;   // start at top

        // -------------------------------------------------
        // DRAW TEXT first
        // -------------------------------------------------
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.setLeading(14.5f);
        cs.newLineAtOffset(margin, cursorY);

        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String extracted = extractor.getText();   // respects paragraph breaks

        // ---- SANITISE ---- (remove TAB, CR and other control chars,
        //                     replace non‑WinAnsi chars with '?')
        String safe = cleanTextForPdf(extracted);
        String[] lines = safe.split("\n");
        for (String line : lines) {
            cs.showText(line);
            cs.newLine();
        }
        cs.endText();

        // Update cursorY so that logos are drawn **below** the text
        cursorY -= lines.length * 14.5f + 30f;   // 30pt extra padding

        // -------------------------------------------------
        // 3b – DRAW LOGOS (appended after the text)
        // -------------------------------------------------
        List<String> logos = tmpl.getLogoPaths();
        if (logos == null || logos.isEmpty()) {
            // fallback to global merchant logo (single logo)
            MerchantInfo mi = new MerchantInfoService().load();
            if (mi.getLogoPath() != null && !mi.getLogoPath().isBlank())
                logos = List.of(mi.getLogoPath());
        }
        if (logos != null) {
            for (String logoPath : logos) {
                Path p = Paths.get(logoPath);
                if (!Files.isRegularFile(p)) continue;
                PDImageXObject img = PDImageXObject.createFromFileByContent(p.toFile(), pdf);

                // Scale to max width 150pt, preserve aspect ratio
                float scale = Math.min(150f / img.getWidth(), 150f / img.getHeight());
                float w = img.getWidth() * scale;
                float h = img.getHeight() * scale;

                cursorY -= h;                     // move down for the image
                cs.drawImage(img, margin, cursorY, w, h);
                cursorY -= 15f;                   // spacing after each logo
            }
        }

        cs.close();

        // -------------------------------------------------
        // Return PDF bytes
        // -------------------------------------------------
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            pdf.save(out);
            return out.toByteArray();
        } finally {
            pdf.close();
            doc.close();
        }
    }

    /* -------------------------------------------------------------
       plain‑text renderer
       ------------------------------------------------------------- */
    /**
     * Render the template as a UTF‑8 plain‑text file.
     *
     * The body is processed with the same placeholder replacement that
     * {@link #renderAsDocx(Template,Map)} and {@link #renderAsPdf(Template,Map)}
     * use.  Logos are ignored – a text file cannot embed images.
     *
     * @param tmpl          the template (new or persisted)
     * @param placeholders  map of ${PLACEHOLDER} → value
     * @return the UTF‑8 encoded bytes of the resulting text
     * @throws IOException  (kept for API symmetry – never thrown in practice)
     */
    public byte[] renderAsText(Template tmpl,
                               Map<String, String> placeholders) throws IOException {
        String body = tmpl.getContent() == null ? "" : tmpl.getContent();

        // Apply the same placeholder logic as the DOCX renderer
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                body = body.replace("${" + e.getKey() + "}", e.getValue());
            }
        }

        // Return UTF‑8 bytes – the UI will write them directly to a .txt file
        return body.getBytes(StandardCharsets.UTF_8);
    }

    //Helps load templates from start
    public void syncTemplatesWithFilesystem() {
        // Load both sides
        List<Template> dbTemplates   = dao.findAll();
        List<Template> fileTemplates = fileDao.findAll();

        // Index DB by name (name is unique in the UI)
        Map<String, Template> dbByName = dbTemplates.stream()
                .collect(Collectors.toMap(Template::getName, t -> t, (a,b) -> a));

        // Insert every file‑only template into the DB
        for (Template fileTpl : fileTemplates) {
            if (!dbByName.containsKey(fileTpl.getName())) {
                // Insert → gets a new id → rewrite the file with the correct name
                boolean inserted = dao.insert(fileTpl);
                if (inserted) {
                    fileDao.save(fileTpl);   // overwrite using the newly assigned id
                }
            }
        }

        // Ensure every DB template has a file representation
        for (Template dbTpl : dbTemplates) {
            if (!fileDao.exists(dbTpl.getId())) {
                fileDao.save(dbTpl);
            }
        }
    }

    /* -----------------------------------------------------------------
       Helper – add a single logo as its own paragraph (used for DOCX)
       ----------------------------------------------------------------- */
    private void addLogoParagraph(XWPFDocument doc, String logoPath) throws IOException {
        Path p = Paths.get(logoPath);
        if (!Files.isRegularFile(p)) {
            System.err.println("Logo file not found: " + logoPath);
            return;
        }
        byte[] bytes = Files.readAllBytes(p);
        String fileName = p.getFileName().toString();

        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();
        try (ByteArrayInputStream pic = new ByteArrayInputStream(bytes)) {
            int pictureType = Document.PICTURE_TYPE_PNG;
            int width = Units.toEMU(100);
            int height = Units.toEMU(100);
            run.addPicture(pic, pictureType, fileName, width, height);
        } catch (InvalidFormatException e) {
            throw new IOException("Invalid picture format", e);
        }
        forceArialFont(run);
    }

    /* -----------------------------------------------------------------
       Remove TAB, carriage‑return and other illegal characters for
       PDFBox Helvetica.
       ----------------------------------------------------------------- */
    private String cleanTextForPdf(String raw) {
        // Remove all carriage‑returns – PDFBox cannot encode U+000D.
        String withoutCR = raw.replace("\r", "");

        // Replace TAB with four spaces and drop any other control chars
        //     (< 32).  Replace any char > 127 (non‑WinAnsi) with '?'.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < withoutCR.length(); i++) {
            char c = withoutCR.charAt(i);
            if (c == '\t') {
                sb.append("    ");
            } else if (c == '\n') {
                sb.append('\n');          // keep line‑feed
            } else if (c < 32) {
                // ignore other ISO‑control characters
            } else if (c > 127) {
                sb.append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------
    // Public shortcuts used by the GUI (they delegate to the methods above)
    // -----------------------------------------------------------------
    public byte[] renderTemplate(int templateId, Map<String,String> placeholders) throws IOException {
        Template tmpl = getTemplate(templateId);
        if (tmpl == null) throw new IllegalArgumentException("Template not found");
        return renderAsDocx(tmpl, placeholders);
    }

    public byte[] renderTemplate(Template tmpl, Map<String,String> placeholders) throws IOException {
        return renderAsDocx(tmpl, placeholders);
    }
}
