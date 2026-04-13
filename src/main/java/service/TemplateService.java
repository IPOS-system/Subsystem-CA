package service;

import dao.TemplateDAO;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service layer – CRUD, DOCX rendering and PDF export.
 *
 * <ul>
 *   <li>Plain‑text templates can contain {@code ${LOGO}} (multiple logos)
 *       and {@code ${TABLE}} (CSV data). </li>
 *   <li>All runs are forced to Arial (fallback font). </li>
 *   <li>PDF export sanitises the extracted text – replaces TABs with
 *       four spaces, strips other control characters and replaces any
 *       non‑WinAnsi glyph with ‘?’. </li>
 *   <li>If the template has *no* logos and *no* table we skip the POI
 *       step and write the text straight to PDF – this avoids the
 *       “NotOfficeXmlFileException”. </li>
 * </ul>
 */
public class TemplateService {

    private final TemplateDAO dao = new TemplateDAO();

    /** Force Arial on every POI run – eliminates missing‑font symbols. */
    private void forceArialFont(XWPFRun run) {
        run.setFontFamily("Arial");
    }

    /* -----------------------------------------------------------------
       CRUD helpers
       ----------------------------------------------------------------- */
    public List<Template> listTemplates() { return dao.findAll(); }

    public Template getTemplate(int id) { return dao.findById(id); }

    public boolean saveTemplate(Template tmpl) {
        if (tmpl.getName() == null || tmpl.getName().trim().isEmpty())
            throw new IllegalArgumentException("Template name cannot be empty");
        if (tmpl.getType() == null || tmpl.getType().trim().isEmpty())
            throw new IllegalArgumentException("Template type cannot be empty");
        return (tmpl.getId() == null) ? dao.insert(tmpl) : dao.update(tmpl);
    }

    public boolean deleteTemplate(int id) { return dao.delete(id); }

    /* -----------------------------------------------------------------
       PUBLIC RENDERERS (used by the GUI)
       ----------------------------------------------------------------- */
    /**
     * Render a template as a DOCX byte[] (logos & tables are inserted,
     * placeholders are replaced, Arial is forced).
     *
     * If the template does **not** need a logo or a table we return a
     * plain UTF‑8 byte[] (the caller may treat it as plain text).
     */
    public byte[] renderAsDocx(Template tmpl, Map<String,String> placeholders) throws IOException {
        // -------------------------------------------------
        // 1️⃣ Plain‑text case – possible multiple logos & a table
        // -------------------------------------------------
        boolean isPlain = tmpl.getBinaryContent() == null &&
                (tmpl.getFilePath() == null || tmpl.getFilePath().isBlank());

        if (isPlain) {
            // ----- replace ordinary placeholders -----
            String rendered = tmpl.getContent() == null ? "" : tmpl.getContent();
            for (Map.Entry<String,String> e : placeholders.entrySet())
                rendered = rendered.replace("${" + e.getKey() + "}", e.getValue());

            boolean needsDocx = (tmpl.getLogoPaths() != null && !tmpl.getLogoPaths().isEmpty())
                    || (tmpl.getTableData() != null && !tmpl.getTableData().isBlank());

            if (!needsDocx) {
                // No logos / tables → just plain UTF‑8 bytes
                return rendered.getBytes(StandardCharsets.UTF_8);
            }

            // -------------------------------------------------
            // Build a tiny DOCX that contains logos, the table,
            // and the rendered text (preserving line‑breaks).
            // -------------------------------------------------
            XWPFDocument doc = new XWPFDocument();

            // ----- LOGOS (via ${LOGO}) -----
            List<String> logos = tmpl.getLogoPaths() != null ? tmpl.getLogoPaths() : List.of();
            AtomicInteger logoIdx = new AtomicInteger(0);
            // Split on the placeholder – keep empty trailing part
            String[] parts = rendered.split("\\$\\{LOGO\\}", -1);
            for (int i = 0; i < parts.length; i++) {
                // text part
                if (!parts[i].isEmpty()) {
                    XWPFParagraph p = doc.createParagraph();
                    XWPFRun r = p.createRun();
                    r.setText(parts[i]);
                    forceArialFont(r);
                }
                // logo after the part (except after the last split part)
                if (i < parts.length - 1 && logoIdx.get() < logos.size()) {
                    addLogoParagraph(doc, logos.get(logoIdx.getAndIncrement()));
                }
            }

            // ----- TABLE (if present) -----
            if (tmpl.getTableData() != null && !tmpl.getTableData().isBlank()) {
                // Look for a ${TABLE} placeholder first – replace it, otherwise append at the end.
                boolean tableHandled = false;
                for (XWPFParagraph p : doc.getParagraphs()) {
                    for (XWPFRun r : p.getRuns()) {
                        String txt = r.getText(0);
                        if (txt != null && txt.contains("${TABLE}")) {
                            txt = txt.replace("${TABLE}", "");
                            r.setText(txt, 0);
                            insertTableAfterParagraph(doc, p, tmpl.getTableData());
                            tableHandled = true;
                            break;
                        }
                    }
                    if (tableHandled) break;
                }
                if (!tableHandled) {
                    // Append after the last paragraph
                    XWPFParagraph last = doc.getParagraphs()
                            .isEmpty() ? doc.createParagraph()
                            : doc.getParagraphs()
                              .get(doc.getParagraphs().size() - 1);
                    insertTableAfterParagraph(doc, last, tmpl.getTableData());
                }
            }

            // -------------------------------------------------
            // Write out DOCX
            // -------------------------------------------------
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                doc.write(out);
                return out.toByteArray();
            } finally {
                doc.close();
            }
        }

        // -------------------------------------------------
        // 2️⃣ .docx template case (binaryContent or file_path)
        // -------------------------------------------------
        InputStream docStream;
        if (tmpl.getBinaryContent() != null) {
            docStream = new ByteArrayInputStream(tmpl.getBinaryContent());
        } else {
            File f = new File(tmpl.getFilePath());
            if (!f.exists())
                throw new FileNotFoundException("Template file not found: " + tmpl.getFilePath());
            docStream = new FileInputStream(f);
        }

        XWPFDocument doc = new XWPFDocument(docStream);

        // 2a – replace ordinary placeholders (forcing Arial)
        for (XWPFParagraph p : doc.getParagraphs())
            replaceInParagraph(p, placeholders);
        for (XWPFTable tbl : doc.getTables())
            for (XWPFTableRow row : tbl.getRows())
                for (XWPFTableCell cell : row.getTableCells())
                    for (XWPFParagraph p : cell.getParagraphs())
                        replaceInParagraph(p, placeholders);

        // 2b – embed logos (order matters)
        List<String> logos = tmpl.getLogoPaths();
        if (logos == null || logos.isEmpty()) {
            // fallback to global merchant logo (single logo)
            MerchantInfo mi = new MerchantInfoService().load();
            if (mi.getLogoPath() != null && !mi.getLogoPath().isBlank())
                logos = List.of(mi.getLogoPath());
        }
        if (logos != null && !logos.isEmpty())
            embedMultipleLogos(doc, logos);

        // 2c – embed table if placeholder exists
        if (tmpl.getTableData() != null && !tmpl.getTableData().isBlank()) {
            boolean inserted = false;
            for (XWPFParagraph p : doc.getParagraphs()) {
                for (XWPFRun r : p.getRuns()) {
                    String txt = r.getText(0);
                    if (txt != null && txt.contains("${TABLE}")) {
                        txt = txt.replace("${TABLE}", "");
                        r.setText(txt, 0);
                        insertTableAfterParagraph(doc, p, tmpl.getTableData());
                        inserted = true;
                        break;
                    }
                }
                if (inserted) break;
            }
            if (!inserted) {
                // Append after the last paragraph
                XWPFParagraph last = doc.getParagraphs()
                        .isEmpty() ? doc.createParagraph()
                        : doc.getParagraphs()
                          .get(doc.getParagraphs().size() - 1);
                insertTableAfterParagraph(doc, last, tmpl.getTableData());
            }
        }

        // -------------------------------------------------
        // Write out the modified document
        // -------------------------------------------------
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.write(out);
            return out.toByteArray();
        } finally {
            doc.close();
            docStream.close();
        }
    }

    /**
     * Render a template as a PDF (logos, text and tables are drawn).
     *
     * If the template does **not** need a logo or a table we skip the
     * POI step entirely and stream the plain text directly to PDF.
     */
    public byte[] renderAsPdf(Template tmpl, Map<String,String> placeholders) throws IOException {
        // -------------------------------------------------
        // Render as DOCX (may be plain‑text bytes)
        // -------------------------------------------------
        byte[] docxBytes = renderAsDocx(tmpl, placeholders);

        // -------------------------------------------------
        // If the result is *not* a DOCX (plain‑text only)
        //     → skip POI completely
        // -------------------------------------------------
        if (!isDocxFile(docxBytes)) {
            String plain = new String(docxBytes, StandardCharsets.UTF_8);
            return createPdfFromPlainText(plain, tmpl);
        }

        // -------------------------------------------------
        // Real DOCX → load with POI, extract text, draw logos & table
        // -------------------------------------------------
        XWPFDocument doc;
        try (InputStream is = new ByteArrayInputStream(docxBytes)) {
            doc = new XWPFDocument(is);
        }

        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        pdf.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(pdf, page);

        float margin = 50f;
        float cursorY = page.getMediaBox().getHeight() - margin;   // start at top

        // -------------------------------------------------
        //  DRAW LOGOS (if any)
        // -------------------------------------------------
        List<String> logos = tmpl.getLogoPaths();
        if (logos == null || logos.isEmpty()) {
            MerchantInfo mi = new MerchantInfoService().load();
            if (mi.getLogoPath() != null && !mi.getLogoPath().isBlank())
                logos = List.of(mi.getLogoPath());
        }
        if (logos != null) {
            for (String logoPath : logos) {
                Path p = Paths.get(logoPath);
                if (!Files.isRegularFile(p)) continue;   // safety
                PDImageXObject img = PDImageXObject.createFromFileByContent(p.toFile(), pdf);

                // Scale to a max width of 150 pt, keep aspect ratio
                float scale = Math.min(150f / img.getWidth(), 150f / img.getHeight());
                float w = img.getWidth() * scale;
                float h = img.getHeight() * scale;

                cursorY -= h;                                 // move down for image
                cs.drawImage(img, margin, cursorY, w, h);
                cursorY -= 15f;                               // a little space after logo
            }
        }

        // -------------------------------------------------
        // DRAW TEXT (extracted from DOCX)
        // -------------------------------------------------
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.setLeading(14.5f);
        cs.newLineAtOffset(margin, cursorY);

        XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
        String extracted = extractor.getText();          // respects paragraph breaks

        // ---------- SANITISE ----------
        String safe = cleanTextForPdf(extracted);       // removes TAB, controls, non‑WinAnsi
        String[] lines = safe.split("\n");
        for (String line : lines) {
            cs.showText(line);
            cs.newLine();
        }
        cs.endText();

        // Move cursor down for the optional CSV table that we will draw next
        cursorY -= lines.length * 14.5f + 30f;          // extra padding

        // -------------------------------------------------
        // DRAW CSV TABLE (if the user supplied one)
        // -------------------------------------------------
        if (tmpl.getTableData() != null && !tmpl.getTableData().isBlank()) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.setLeading(14.5f);
            cs.newLineAtOffset(margin, cursorY);

            String[] rows = tmpl.getTableData().split("\\r?\\n");
            for (String row : rows) {
                // Keep empty cells – split with -1
                String[] cols = row.split(",", -1);
                // TAB → 4 spaces to keep PDFBox happy
                String rowText = String.join("    ", cols);
                cs.showText(rowText);
                cs.newLine();
            }
            cs.endText();
        }

        cs.close();

        // -------------------------------------------------
        // Return the PDF bytes
        // -------------------------------------------------
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            pdf.save(out);
            return out.toByteArray();
        } finally {
            pdf.close();
            doc.close();
        }
    }

    /* -----------------------------------------------------------------
       Helper – write a plain‑text PDF when the template does not need DOCX
       ----------------------------------------------------------------- */
    private byte[] createPdfFromPlainText(String plain, Template tmpl) throws IOException {
        PDDocument pdf = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        pdf.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(pdf, page);

        float margin = 50f;
        float cursorY = page.getMediaBox().getHeight() - margin;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.setLeading(14.5f);
        cs.newLineAtOffset(margin, cursorY);

        // Sanitise the text (removes TAB etc.)
        String safe = cleanTextForPdf(plain);
        String[] lines = safe.split("\n");
        for (String line : lines) {
            cs.showText(line);
            cs.newLine();
        }
        cs.endText();

        cs.close();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            pdf.save(out);
            return out.toByteArray();
        } finally {
            pdf.close();
        }
    }

    /* -----------------------------------------------------------------
       Does the byte[] look like a DOCX (a ZIP file)?
       ----------------------------------------------------------------- */
    private boolean isDocxFile(byte[] data) {
        // DOCX files are ZIP archives – first two bytes are PK (0x50 0x4B)
        return data != null && data.length >= 2 && data[0] == 0x50 && data[1] == 0x4B;
    }

    /* -----------------------------------------------------------------
       Helper – replace placeholders inside a paragraph & force Arial
       ----------------------------------------------------------------- */
    private void replaceInParagraph(XWPFParagraph p, Map<String,String> placeholders) {
        for (XWPFRun run : p.getRuns()) {
            String txt = run.getText(0);
            if (txt != null) {
                for (Map.Entry<String,String> e : placeholders.entrySet())
                    txt = txt.replace("${" + e.getKey() + "}", e.getValue());
                run.setText(txt, 0);
            }
            forceArialFont(run);
        }
    }

    /* -----------------------------------------------------------------
       LOGO EMBEDDING – multiple logos (order matters)
       ----------------------------------------------------------------- */
    private void embedMultipleLogos(XWPFDocument doc, List<String> logos) throws IOException {
        AtomicInteger idx = new AtomicInteger(0);
        // body paragraphs
        for (XWPFParagraph p : doc.getParagraphs())
            replaceLogoInParagraph(p, logos, idx);
        // inside tables
        for (XWPFTable tbl : doc.getTables())
            for (XWPFTableRow row : tbl.getRows())
                for (XWPFTableCell cell : row.getTableCells())
                    for (XWPFParagraph p : cell.getParagraphs())
                        replaceLogoInParagraph(p, logos, idx);
    }

    /** Replace each {@code ${LOGO}} in a paragraph with the next logo from the list. */
    private void replaceLogoInParagraph(XWPFParagraph para,
                                        List<String> logos,
                                        AtomicInteger idx) throws IOException {
        List<XWPFRun> runs = para.getRuns();
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String txt = run.getText(0);
            if (txt != null && txt.contains("${LOGO}")) {
                String[] parts = txt.split("\\$\\{LOGO\\}", -1);
                run.setText(parts[0], 0);                     // first part stays
                for (int partIdx = 1; partIdx < parts.length; partIdx++) {
                    // insert logo if we still have one left
                    if (idx.get() < logos.size()) {
                        String logoPath = logos.get(idx.getAndIncrement());
                        try (InputStream pic = Files.newInputStream(Paths.get(logoPath))) {
                            int pictureType = Document.PICTURE_TYPE_PNG;
                            int width = Units.toEMU(100);
                            int height = Units.toEMU(100);
                            run.addPicture(pic, pictureType,
                                    Paths.get(logoPath).getFileName().toString(),
                                    width, height);
                        } catch (InvalidFormatException e) {
                            throw new IOException("Invalid picture format", e);
                        }
                    }
                    // insert the following text fragment
                    XWPFRun newRun = para.insertNewRun(i + 1);
                    newRun.setText(parts[partIdx]);
                    forceArialFont(newRun);
                    i++; // step over the newly inserted run
                }
            }
            forceArialFont(run);
        }
    }

    /** Add a single logo as its own paragraph (used when there is no {@code ${LOGO}} placeholder). */
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

    /**
     * Insert a table **after** the given paragraph.
     * The CSV can contain empty cells (e.g. {@code "A,,C"}).
     * Blank lines are ignored.
     */
    private void insertTableAfterParagraph(XWPFDocument doc,
                                           XWPFParagraph after,
                                           String csvData) {
        // ---------- Parse CSV (preserve empty columns) ----------
        String[] rawRows = csvData.split("\\r?\\n");
        List<String[]> rows = new ArrayList<>();
        int maxCols = 0;
        for (String raw : rawRows) {
            if (raw.trim().isEmpty()) continue;          // skip empty line
            String[] cols = raw.split(",", -1);           // keep empty cells
            rows.add(cols);
            if (cols.length > maxCols) maxCols = cols.length;
        }
        if (rows.isEmpty()) return; // nothing to insert

        // ---------- Create the table after the paragraph ----------
        XWPFTable table = doc.insertNewTbl(after.getCTP().newCursor());

        // ---------- Header row ----------
        XWPFTableRow header = table.getRow(0); // first row already exists
        while (header.getTableCells().size() < maxCols) {
            header.createCell();
        }
        String[] headerCols = rows.get(0);
        for (int c = 0; c < maxCols; c++) {
            XWPFTableCell cell = header.getCell(c);
            XWPFParagraph p = cell.getParagraphs().get(0);
            XWPFRun r = p.createRun();
            r.setBold(true);
            String txt = c < headerCols.length ? headerCols[c].trim() : "";
            r.setText(txt);
            forceArialFont(r);
        }

        // ---------- Data rows ----------
        for (int rowIdx = 1; rowIdx < rows.size(); rowIdx++) {
            XWPFTableRow row = table.createRow();
            while (row.getTableCells().size() < maxCols) {
                row.createCell();
            }
            String[] cols = rows.get(rowIdx);
            for (int c = 0; c < maxCols; c++) {
                XWPFTableCell cell = row.getCell(c);
                XWPFParagraph p = cell.getParagraphs().get(0);
                XWPFRun r = p.createRun();
                String txt = c < cols.length ? cols[c].trim() : "";
                r.setText(txt);
                forceArialFont(r);
            }
        }
    }

    /* -----------------------------------------------------------------
       PDF TEXT‑CLEAN‑UP – removes every character that Helvetica
       cannot encode (TAB, control chars, non‑WinAnsi glyphs).
       ----------------------------------------------------------------- */
    private String cleanTextForPdf(String raw) {
        // Replace TAB with four spaces, keep newline & carriage‑return,
        // drop any other ISO‑control character, replace non‑ASCII with '?'
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\t') {
                sb.append("    ");               // four spaces
            } else if (c == '\n' || c == '\r') {
                sb.append(c);
            } else if (c < 32) {                // other control chars → drop
                // skip
            } else if (c > 127) {               // non‑WinAnsi glyph → replace by '?'
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
