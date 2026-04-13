package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO that maps a row in the {@code Templates} table.
 *
 * <p>
 *   • {@code content} – plain‑text body (may be {@code null})<br>
 *   • {@code logoPaths} – ordered list of absolute PNG paths (may be empty)<br>
 * </p>
 *
 * The old Word‑doc fields (`binaryContent`, `filePath`) and the CSV
 * table field (`tableData`) have been removed because the UI no longer
 * supports them.
 */
public class Template {

    private Integer id;                // DB PK – null for a brand‑new template
    private String  name;              // display name (must be unique)
    private String  type;              // REMINDER / RECEIPT / INVOICE
    private String  content;           // plain‑text body (may be null)
    private List<String> logoPaths = new ArrayList<>(); // ordered PNG list

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

    public List<String> getLogoPaths()        { return logoPaths; }
    public void setLogoPaths(List<String> list){ this.logoPaths = list; }

    public void addLogoPath(String path) {
        if (path != null && !path.isBlank() && !logoPaths.contains(path))
            logoPaths.add(path);
    }

    public void removeLogoPath(String path) {
        logoPaths.remove(path);
    }

    @Override
    public String toString() { return name; }
}
