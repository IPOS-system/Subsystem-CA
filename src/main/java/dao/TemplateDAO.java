package dao;

import api_impl.DatabaseConnection;
import domain.Template;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the {@code Templates} table.
 * Handles the new {@code logo_paths} (CSV) and {@code table_data} columns.
 */
public class TemplateDAO {

    /* ------------------- INSERT ------------------- */
    public boolean insert(Template tmpl) {
        String sql = """
                INSERT INTO Templates
                    (name, type, content, content_blob, file_path, logo_paths, table_data)
                VALUES (?,?,?,?,?,?,?)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tmpl.getName());
            ps.setString(2, tmpl.getType());
            ps.setString(3, tmpl.getContent());

            // BLOB (binary .docx)
            if (tmpl.getBinaryContent() != null) {
                InputStream is = new ByteArrayInputStream(tmpl.getBinaryContent());
                ps.setBlob(4, is);
            } else {
                ps.setNull(4, Types.BLOB);
            }

            // file_path
            if (tmpl.getFilePath() != null) {
                ps.setString(5, tmpl.getFilePath());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            // logo_paths (CSV)
            if (tmpl.getLogoPaths() != null && !tmpl.getLogoPaths().isEmpty()) {
                ps.setString(6, String.join(",", tmpl.getLogoPaths()));
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            // table_data
            if (tmpl.getTableData() != null && !tmpl.getTableData().isBlank()) {
                ps.setString(7, tmpl.getTableData());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }

            int rows = ps.executeUpdate();
            if (rows == 0) return false;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) tmpl.setId(rs.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ------------------- UPDATE ------------------- */
    public boolean update(Template tmpl) {
        if (tmpl.getId() == null) {
            throw new IllegalArgumentException("Template id required for update");
        }

        String sql = """
                UPDATE Templates
                SET name = ?, type = ?, content = ?, content_blob = ?, file_path = ?, logo_paths = ?, table_data = ?
                WHERE template_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tmpl.getName());
            ps.setString(2, tmpl.getType());
            ps.setString(3, tmpl.getContent());

            // BLOB
            if (tmpl.getBinaryContent() != null) {
                InputStream is = new ByteArrayInputStream(tmpl.getBinaryContent());
                ps.setBlob(4, is);
            } else {
                ps.setNull(4, Types.BLOB);
            }

            // file_path
            if (tmpl.getFilePath() != null) {
                ps.setString(5, tmpl.getFilePath());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            // logo_paths (CSV)
            if (tmpl.getLogoPaths() != null && !tmpl.getLogoPaths().isEmpty()) {
                ps.setString(6, String.join(",", tmpl.getLogoPaths()));
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            // table_data
            if (tmpl.getTableData() != null && !tmpl.getTableData().isBlank()) {
                ps.setString(7, tmpl.getTableData());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }

            ps.setInt(8, tmpl.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ------------------- DELETE ------------------- */
    public boolean delete(int id) {
        String sql = "DELETE FROM Templates WHERE template_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ------------------- FIND BY ID ------------------- */
    public Template findById(int id) {
        String sql = """
                SELECT template_id, name, type, content, content_blob, file_path, logo_paths, table_data
                FROM Templates
                WHERE template_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Template t = new Template();
                    t.setId(rs.getInt("template_id"));
                    t.setName(rs.getString("name"));
                    t.setType(rs.getString("type"));
                    t.setContent(rs.getString("content"));
                    t.setFilePath(rs.getString("file_path"));
                    t.setTableData(rs.getString("table_data"));

                    // logo_paths CSV
                    String logosCsv = rs.getString("logo_paths");
                    if (logosCsv != null && !logosCsv.isBlank()) {
                        List<String> list = new ArrayList<>();
                        for (String p : logosCsv.split(",")) {
                            if (!p.isBlank())
                                list.add(p.trim());
                        }
                        t.setLogoPaths(list);
                    }

                    // BLOB
                    Blob blob = rs.getBlob("content_blob");
                    if (blob != null) {
                        t.setBinaryContent(blob.getBytes(1, (int) blob.length()));
                    }
                    return t;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* ------------------- FIND ALL (list view) ------------------- */
    public List<Template> findAll() {
        List<Template> list = new ArrayList<>();
        String sql = """
                SELECT template_id, name, type, content, file_path, logo_paths, table_data
                FROM Templates
                ORDER BY name
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Template t = new Template();
                t.setId(rs.getInt("template_id"));
                t.setName(rs.getString("name"));
                t.setType(rs.getString("type"));
                t.setContent(rs.getString("content"));
                t.setFilePath(rs.getString("file_path"));
                t.setTableData(rs.getString("table_data"));

                // logo_paths
                String logosCsv = rs.getString("logo_paths");
                if (logosCsv != null && !logosCsv.isBlank()) {
                    List<String> pathList = new ArrayList<>();
                    for (String p : logosCsv.split(",")) {
                        if (!p.isBlank())
                            pathList.add(p.trim());
                    }
                    t.setLogoPaths(pathList);
                }

                // binaryContent stays null – fetched only on demand via findById()
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
