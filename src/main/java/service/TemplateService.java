package service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class TemplateService {

    private final Path dataDir;

    public TemplateService() {
        this(Paths.get("src", "main", "resources", "templates"));
    }

    public TemplateService(Path dataDir) {
        this.dataDir = Objects.requireNonNull(dataDir);
    }

    private Path getTemplatePath(String name) {
        return dataDir.resolve(name + ".txt");
    }

    private Path getValuesPath(String name) {
        return dataDir.resolve(name + ".properties");
    }

    public void ensureTemplateExists(String name, String defaultTemplate) throws IOException {
        Files.createDirectories(dataDir);

        Path file = getTemplatePath(name);
        if (Files.notExists(file)) {
            Files.writeString(file, defaultTemplate, StandardOpenOption.CREATE_NEW);
        }
    }

    public String loadTemplate(String name) throws IOException {
        return Files.readString(getTemplatePath(name));
    }

    public void saveTemplate(String name, String template) throws IOException {
        Files.createDirectories(dataDir);
        Files.writeString(
                getTemplatePath(name),
                template,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public Map<String, String> loadValues(String name) throws IOException {
        Map<String, String> values = new LinkedHashMap<>();
        Path file = getValuesPath(name);

        if (Files.notExists(file)) {
            return values;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }

        for (String key : properties.stringPropertyNames()) {
            values.put(key, properties.getProperty(key, ""));
        }

        return values;
    }

    public void saveValues(String name, Map<String, String> values) throws IOException {
        Files.createDirectories(dataDir);

        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
        }

        try (Writer writer = Files.newBufferedWriter(
                getValuesPath(name),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            properties.store(writer, "Saved values for " + name);
        }
    }

    public void resetValues(String name) throws IOException {
        Files.deleteIfExists(getValuesPath(name));
    }

    public String applyValues(String template, Map<String, String> values) {
        String result = template;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace(placeholder, value);
        }

        return result;
    }
}