package domain;


public class Template {

    private Integer id;
    // null for a brand‑new template
    private String name;
    // display name, must be unique
    private String type;
    // REMINDER, RECEIPT, INVOICE …
    private String content;
    // plain‑text body (may be null)
    private byte[] binaryContent;
    // raw .docx bytes (may be null)
    private String filePath;
    // UI‑only – not persisted

    public Template() {}


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public byte[] getBinaryContent() { return binaryContent; }
    public void setBinaryContent(byte[] binaryContent) { this.binaryContent = binaryContent; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    @Override
    public String toString() {
        // JList uses this to display the name
        return name;
    }
}
