package dao;

import domain.Template;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Simple file‑based DAO that stores one Template per “template_<id>.ser”
 * inside the folder {@code src/main/resources/templates} (or any folder you
 * prefer).  It mirrors the same CRUD‑like API you already have for the DB.
 */
public class TemplateFileDAO {

    /** Change this if you really want the folder inside the JAR. */
    private static final Path TEMPLATE_DIR = Paths.get(
            System.getProperty("user.dir"),
            "src", "main", "resources", "templates");

    public TemplateFileDAO() {
        try {
            Files.createDirectories(TEMPLATE_DIR);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not create template directory: " + TEMPLATE_DIR, e);
        }
    }

    /** File name is based on the DB primary key – stable across edits. */
    private Path pathFor(Template tmpl) {
        if (tmpl.getId() == null) {
            throw new IllegalArgumentException("Template id is required for file naming");
        }
        return TEMPLATE_DIR.resolve("template_" + tmpl.getId() + ".ser");
    }

    /** Persist a Template to disk (overwrites any existing file). */
    public boolean save(Template tmpl) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(pathFor(tmpl)))) {
            oos.writeObject(tmpl);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Delete the file belonging to the supplied Template. */
    public boolean delete(Template tmpl) {
        try {
            return Files.deleteIfExists(pathFor(tmpl));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Does a file for the given id already exist? */
    public boolean exists(int id) {
        return Files.exists(TEMPLATE_DIR.resolve("template_" + id + ".ser"));
    }

    /** Load **all** template files that are present on disk. */
    public List<Template> findAll() {
        List<Template> list = new ArrayList<>();
        try (Stream<Path> stream = Files.list(TEMPLATE_DIR)) {
            List<Path> files = stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".ser"))
                    .collect(Collectors.toList());

            for (Path file : files) {
                try (ObjectInputStream ois = new ObjectInputStream(
                        Files.newInputStream(file))) {
                    Object obj = ois.readObject();
                    if (obj instanceof Template) {
                        list.add((Template) obj);
                    }
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();   // corrupted file → ignore
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
