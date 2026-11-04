import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class GameCategoryStorage {
    private final File storageFile;

    public GameCategoryStorage(String filePath) {
        storageFile = new File(filePath);
    }

    public GameCategories load() throws IOException {
        GameCategories categories = new GameCategories();
        if (!storageFile.exists()) {
            return categories;
        }

        Properties properties = new Properties();
        try (java.io.Reader reader = Files.newBufferedReader(
                storageFile.toPath(), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        for (GameCategories.Category category : GameCategories.Category.values()) {
            String prefix = category.name() + ".";
            String countValue = properties.getProperty(prefix + "count");
            if (countValue == null) {
                continue;
            }

            int count;
            try {
                count = Integer.parseInt(countValue);
            } catch (NumberFormatException e) {
                throw new IOException("Ungültige Anzahl für Kategorie " + category + ".", e);
            }
            if (count < 1) {
                throw new IOException("Die Kategorie " + category + " enthält keine Werte.");
            }

            List<GameCategories.Option> loadedOptions = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            Set<String> labels = new HashSet<>();
            for (int i = 0; i < count; i++) {
                String itemPrefix = prefix + i + ".";
                String id = properties.getProperty(itemPrefix + "id");
                String label = properties.getProperty(itemPrefix + "label");
                if (id == null || label == null || id.isBlank() || label.isBlank()) {
                    throw new IOException("Ungültiger Eintrag in Kategorie " + category + ".");
                }
                if (!ids.add(id) || !labels.add(label.toLowerCase(java.util.Locale.ROOT))) {
                    throw new IOException("Doppelte Werte in Kategorie " + category + ".");
                }
                loadedOptions.add(new GameCategories.Option(id, label));
            }
            try {
                categories.setOptions(category, loadedOptions);
            } catch (IllegalArgumentException e) {
                throw new IOException("Ungültige Kategorie " + category + ".", e);
            }
        }
        return categories;
    }

    public void save(GameCategories categories) throws IOException {
        File parent = storageFile.getAbsoluteFile().getParentFile();
        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }

        Properties properties = new Properties();
        for (GameCategories.Category category : GameCategories.Category.values()) {
            String prefix = category.name() + ".";
            properties.setProperty(prefix + "count",
                    Integer.toString(categories.options(category).size()));
            for (int i = 0; i < categories.options(category).size(); i++) {
                GameCategories.Option option = categories.options(category).get(i);
                String itemPrefix = prefix + i + ".";
                properties.setProperty(itemPrefix + "id", option.getId());
                properties.setProperty(itemPrefix + "label", option.getLabel());
            }
        }

        File tempFile = File.createTempFile("spielekategorien-", ".tmp", parent);
        try {
            try (java.io.Writer writer = Files.newBufferedWriter(
                    tempFile.toPath(), StandardCharsets.UTF_8)) {
                properties.store(writer, "Spielebibliothek-Kategorien");
            }
            try {
                Files.move(tempFile.toPath(), storageFile.toPath(),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), storageFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }
}
