package dao;

import api_impl.DatabaseConnection;
import domain.Template;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class TemplateDAO {


    public boolean insert(Template tmpl) {
        String sql = """
                INSERT INTO Templates (name, type, content, content_blob)
                VALUES (?,?,?,?)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tmpl.getName());
            ps.setString(2, tmpl.getType());
            ps.setString(3, tmpl.getContent()); // may be null

            if (tmpl.getBinaryContent() != null) {
                InputStream is = new ByteArrayInputStream(tmpl.getBinaryContent());
                ps.setBlob(4, is);
            } else {
                ps.setNull(4, Types.BLOB);
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


    public boolean update(Template tmpl) {
        if (tmpl.getId() == null) {
            throw new IllegalArgumentException("Template id required for update");
        }
        String sql = """
                UPDATE Templates
                SET name = ?, type = ?, content = ?, content_blob = ?
                WHERE template_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tmpl.getName());
            ps.setString(2, tmpl.getType());
            ps.setString(3, tmpl.getContent());

            if (tmpl.getBinaryContent() != null) {
                InputStream is = new ByteArrayInputStream(tmpl.getBinaryContent());
                ps.setBlob(4, is);
            } else {
                ps.setNull(4, Types.BLOB);
            }

            ps.setInt(5, tmpl.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


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

    //blob
    public Template findById(int id) {
        String sql = """
                SELECT template_id, name, type, content, content_blob
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

    public List<Template> findAll() {
        List<Template> list = new ArrayList<>();
        String sql = "SELECT template_id, name, type, content FROM Templates ORDER BY name";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Template t = new Template();
                t.setId(rs.getInt("template_id"));
                t.setName(rs.getString("name"));
                t.setType(rs.getString("type"));
                t.setContent(rs.getString("content"));
                // binaryContent stays null – fetched only when needed via findById()
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
