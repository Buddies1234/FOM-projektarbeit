import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

public final class GameCategories {
    public enum Category {
        PLATFORM("Plattform"),
        DISTRIBUTION_TYPE("Format"),
        STOREFRONT("Store"),
        CONDITION("Zustand"),
        PLAY_STATUS("Spielstatus");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static final class Option {
        private final String id;
        private final String label;

        Option(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final EnumMap<Category, List<Option>> options = new EnumMap<>(Category.class);

    public GameCategories() {
        addDefaults(Category.PLATFORM, new String[][] {
            {"PC", "PC"}, {"PLAYSTATION_5", "PlayStation 5"},
            {"PLAYSTATION_4", "PlayStation 4"}, {"PLAYSTATION_3", "PlayStation 3"},
            {"PLAYSTATION_RETRO", "PlayStation (Retro)"}, {"NINTENDO_SWITCH", "Nintendo Switch"},
            {"NINTENDO_RETRO", "Nintendo (Retro)"}, {"XBOX_SERIES", "Xbox Series"},
            {"XBOX_ONE", "Xbox One"}, {"XBOX_RETRO", "Xbox (Retro)"}, {"OTHER", "Sonstige"}
        });
        addDefaults(Category.DISTRIBUTION_TYPE, new String[][] {
            {"PHYSICAL", "Physisch"}, {"DIGITAL", "Digital"}
        });
        addDefaults(Category.STOREFRONT, new String[][] {
            {"NONE", "Keiner"}, {"STEAM", "Steam"}, {"GOG", "GOG"},
            {"EPIC_GAMES", "Epic Games"}, {"PLAYSTATION_NETWORK", "PlayStation Network"},
            {"NINTENDO_ESHOP", "Nintendo eShop"}, {"XBOX_STORE", "Xbox Store"},
            {"OTHER", "Sonstiger Store"}
        });
        addDefaults(Category.CONDITION, new String[][] {
            {"NEW_SEALED", "Neu / OVP"}, {"COMPLETE_IN_BOX", "Vollständig mit Hülle"},
            {"GOOD", "Gut"}, {"LOOSE", "Nur Modul/Disc"},
            {"NOT_APPLICABLE", "Nicht zutreffend"}
        });
        addDefaults(Category.PLAY_STATUS, new String[][] {
            {"UNPLAYED", "Noch nicht gespielt"}, {"PLAYING", "Wird gespielt"},
            {"BEATEN", "Durchgespielt"}, {"COMPLETED_100", "Zu 100 % abgeschlossen"},
            {"DROPPED", "Abgebrochen"}
        });
    }

    public GameCategories(GameCategories source) {
        for (Category category : Category.values()) {
            options.put(category, new ArrayList<>(source.options(category)));
        }
    }

    public List<Option> options(Category category) {
        List<Option> categoryOptions = options.get(category);
        return categoryOptions == null
                ? Collections.emptyList() : Collections.unmodifiableList(categoryOptions);
    }

    public String label(Category category, String id) {
        Option option = find(category, id);
        return option == null ? (id == null ? "-" : id) : option.getLabel();
    }

    public Option find(Category category, String id) {
        if (id == null) {
            return null;
        }
        for (Option option : options(category)) {
            if (option.getId().equals(id)) {
                return option;
            }
        }
        return null;
    }

    void setOptions(Category category, List<Option> newOptions) {
        if (newOptions.isEmpty()) {
            throw new IllegalArgumentException("Eine Kategorie muss mindestens einen Wert enthalten.");
        }
        options.put(category, new ArrayList<>(newOptions));
    }

    public void add(Category category, String label) {
        String normalized = normalizeLabel(label);
        ensureUnique(category, normalized, null);
        options.get(category).add(new Option(UUID.randomUUID().toString(), normalized));
    }

    public void rename(Category category, String id, String label) {
        String normalized = normalizeLabel(label);
        ensureUnique(category, normalized, id);
        List<Option> categoryOptions = options.get(category);
        for (int i = 0; i < categoryOptions.size(); i++) {
            Option option = categoryOptions.get(i);
            if (option.getId().equals(id)) {
                categoryOptions.set(i, new Option(id, normalized));
                return;
            }
        }
        throw new IllegalArgumentException("Der ausgewählte Eintrag wurde nicht gefunden.");
    }

    public void remove(Category category, String id) {
        if (options(category).size() <= 1) {
            throw new IllegalArgumentException("Eine Kategorie muss mindestens einen Wert enthalten.");
        }
        options.get(category).removeIf(option -> option.getId().equals(id));
    }

    public static boolean isUsed(Category category, String id, List<Game> games) {
        for (Game game : games) {
            String value;
            switch (category) {
                case PLATFORM: value = game.getPlatform(); break;
                case DISTRIBUTION_TYPE: value = game.getDistributionType(); break;
                case STOREFRONT: value = game.getStorefront(); break;
                case CONDITION: value = game.getCondition(); break;
                case PLAY_STATUS: value = game.getStatus(); break;
                default: throw new IllegalStateException("Unbekannte Kategorie: " + category);
            }
            if (id.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void addDefaults(Category category, String[][] defaults) {
        List<Option> categoryOptions = new ArrayList<>();
        for (String[] value : defaults) {
            categoryOptions.add(new Option(value[0], value[1]));
        }
        options.put(category, categoryOptions);
    }

    private void ensureUnique(Category category, String label, String exceptId) {
        for (Option option : options(category)) {
            if (!option.getId().equals(exceptId)
                    && option.getLabel().equalsIgnoreCase(label)) {
                throw new IllegalArgumentException("Dieser Wert ist in der Kategorie bereits vorhanden.");
            }
        }
    }

    private String normalizeLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Der Name darf nicht leer sein.");
        }
        return label.trim();
    }
}
