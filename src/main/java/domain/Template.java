package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO that maps one row in the {@code Templates} table.
 *
 * <p>
 *      • {@code content}        – plain‑text version (optional) <br>
 *      • {@code binaryContent} – raw .docx bytes (optional) <br>
 *      • {@code filePath}      – absolute path to a .docx file (optional) <br>
 *      • {@code logoPaths}     – **ordered** list of absolute PNG paths (optional) <br>
 *      • {@code tableData}     – optional CSV string that describes a table.
 *                                   Rows are separated by a newline,
 *                                   columns by commas.
 *                                   Example:
 *                                   <pre>
 *                                   Header1,Header2
 *                                   Row1Col1,Row1Col2
 *                                   Row2Col1,Row2Col2
 *                                   </pre>
 * </p>
 */
public class Template {

    private Integer id;                // DB PK – null for a brand‑new template
    private String  name;              // display name (must be unique)
    private String  type;              // REMINDER / RECEIPT / INVOICE
    private String  content;           // plain‑text body (may be null)
    private byte[]  binaryContent;       // raw .docx bytes (may be null)
    private String  filePath;          // optional .docx file path
    private List<String> logoPaths = new ArrayList<>(); // ordered PNG list
    private String tableData;          // optional CSV text for a table (may be null)

    public Template() {}

    // -------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------
    public Integer getId()                     { return id; }
    public void setId(Integer id)              { this.id = id; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getType()                    { return type; }
    public void setType(String type)           { this.type = type; }

    public String getContent()                 { return content; }
    public void setContent(String content)     { this.content = content; }

    public byte[] getBinaryContent()           { return binaryContent; }
    public void setBinaryContent(byte[] data)  { this.binaryContent = data; }

    public String getFilePath()                { return filePath; }
    public void setFilePath(String path)       { this.filePath = path; }

    public List<String> getLogoPaths()        { return logoPaths; }
    public void setLogoPaths(List<String> list){ this.logoPaths = list; }

    public String getTableData()               { return tableData; }
    public void setTableData(String csv)       { this.tableData = csv; }

    /** Convenience – first logo (or null) */
    public String getFirstLogoPath() {
        return logoPaths.isEmpty() ? null : logoPaths.get(0);
    }

    public void addLogoPath(String path) {
        if (path != null && !path.isBlank() && !logoPaths.contains(path))
            logoPaths.add(path);
    }

    public void removeLogoPath(String path) {
        logoPaths.remove(path);
    }

    // JList uses this to display the name
    @Override
    public String toString() { return name; }
}
