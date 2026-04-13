package dao;

import api_impl.DatabaseConnection;
import domain.Template;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the {@code Templates} table.
 *
 * The table now stores only:
 *   name, type, content (plain‑text) and logo_paths (comma‑separated PNG list).
 * All columns related to Word‑doc binaries and CSV tables have been removed.
 */
public class TemplateDAO {

    /* ------------------- INSERT ------------------- */
    public boolean insert(Template tmpl) {
        String sql = """
                INSERT INTO Templates
                    (name, type, content, logo_paths)
                VALUES (?,?,?,?)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tmpl.getName());
            ps.setString(2, tmpl.getType());
            ps.setString(3, tmpl.getContent());

            // logo_paths (CSV)
            if (tmpl.getLogoPaths() != null && !tmpl.getLogoPaths().isEmpty())
                ps.setString(4, String.join(",", tmpl.getLogoPaths()));
            else
                ps.setNull(4, Types.VARCHAR);

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
                SET name = ?, type = ?, content = ?, logo_paths = ?
                WHERE template_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tmpl.getName());
            ps.setString(2, tmpl.getType());
            ps.setString(3, tmpl.getContent());

            // logo_paths
            if (tmpl.getLogoPaths() != null && !tmpl.getLogoPaths().isEmpty())
                ps.setString(4, String.join(",", tmpl.getLogoPaths()));
            else
                ps.setNull(4, Types.VARCHAR);

            ps.setInt(5, tmpl.getId());

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
                SELECT template_id, name, type, content, logo_paths
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

                    String csv = rs.getString("logo_paths");
                    if (csv != null && !csv.isBlank()) {
                        List<String> list = new ArrayList<>();
                        for (String p : csv.split(",")) {
                            if (!p.isBlank()) list.add(p.trim());
                        }
                        t.setLogoPaths(list);
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
                SELECT template_id, name, type, content, logo_paths
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

                String csv = rs.getString("logo_paths");
                if (csv != null && !csv.isBlank()) {
                    List<String> pathList = new ArrayList<>();
                    for (String p : csv.split(",")) {
                        if (!p.isBlank()) pathList.add(p.trim());
                    }
                    t.setLogoPaths(pathList);
                }
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
//hi
