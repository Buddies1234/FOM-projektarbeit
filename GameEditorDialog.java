import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class GameEditorDialog {
    private GameEditorDialog() {
    }

    public static Game showDialog(Component parent, Game original, GameCategories categories) {
        Game draft = copyOf(original);

        JTextField titleField = new JTextField(textOrEmpty(draft.getTitle()), 32);
        JTextField developerField = new JTextField(textOrEmpty(draft.getDeveloper()), 32);
        JTextField releaseYearField = new JTextField(
                Integer.toString(draft.getReleaseYear()), 8);
        JTextField genresField = new JTextField(
                draft.getGenres() == null ? "" : String.join(", ", draft.getGenres()), 32);
        JTextField seriesField = new JTextField(textOrEmpty(draft.getSeries()), 32);
        JComboBox<GameCategories.Option> platformField = createCombo(
                categories, GameCategories.Category.PLATFORM);
        JComboBox<GameCategories.Option> distributionField = createCombo(
                categories, GameCategories.Category.DISTRIBUTION_TYPE);
        JComboBox<GameCategories.Option> storefrontField = createCombo(
                categories, GameCategories.Category.STOREFRONT);
        JComboBox<GameCategories.Option> conditionField = createCombo(
                categories, GameCategories.Category.CONDITION);
        JComboBox<GameCategories.Option> statusField = createCombo(
                categories, GameCategories.Category.PLAY_STATUS);
        JTextField completionDateField = new JTextField(
                draft.getCompletionDate() == null ? "" : draft.getCompletionDate().toString(), 12);
        JTextField ratingField = new JTextField(Integer.toString(draft.getRating()), 8);
        JTextArea notesField = new JTextArea(textOrEmpty(draft.getNotes()), 3, 32);
        notesField.setLineWrap(true);
        notesField.setWrapStyleWord(true);

        selectOption(platformField, categories, GameCategories.Category.PLATFORM,
                draft.getPlatform(), "PC");
        selectOption(distributionField, categories, GameCategories.Category.DISTRIBUTION_TYPE,
                draft.getDistributionType(), "PHYSICAL");
        selectOption(storefrontField, categories, GameCategories.Category.STOREFRONT,
                draft.getStorefront(), "NONE");
        selectOption(conditionField, categories, GameCategories.Category.CONDITION,
                draft.getCondition(), "GOOD");
        selectOption(statusField, categories, GameCategories.Category.PLAY_STATUS,
                draft.getStatus(), "UNPLAYED");

        distributionField.addActionListener(event -> updateDistributionFields(
                distributionField, storefrontField, conditionField));
        updateDistributionFields(distributionField, storefrontField, conditionField);

        JPanel fields = new JPanel(new GridBagLayout());
        int row = 0;
        addRow(fields, row++, "Titel *", titleField);
        addRow(fields, row++, "Entwickler", developerField);
        addRow(fields, row++, "Erscheinungsjahr (0 = unbekannt)", releaseYearField);
        addRow(fields, row++, "Genres (mit Komma trennen)", genresField);
        addRow(fields, row++, "Spielreihe", seriesField);
        addRow(fields, row++, "Plattform *", platformField);
        addRow(fields, row++, "Format *", distributionField);
        addRow(fields, row++, "Store", storefrontField);
        addRow(fields, row++, "Zustand", conditionField);
        addRow(fields, row++, "Spielstatus", statusField);
        addRow(fields, row++, "Abschlussdatum (JJJJ-MM-TT)", completionDateField);
        addRow(fields, row++, "Bewertung (0-10)", ratingField);
        addRow(fields, row, "Notizen", new JScrollPane(notesField));

        JScrollPane scrollPane = new JScrollPane(fields);
        scrollPane.setPreferredSize(new java.awt.Dimension(560, 500));
        String dialogTitle = original.getTitle() == null || original.getTitle().isBlank()
                ? "Spiel anlegen" : "Spiel bearbeiten";

        while (true) {
            int result = JOptionPane.showConfirmDialog(parent, scrollPane, dialogTitle,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            try {
                return applyValues(draft, titleField, developerField, releaseYearField,
                        genresField, seriesField, platformField, distributionField,
                        storefrontField, conditionField, statusField, completionDateField,
                        ratingField, notesField);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(parent, e.getMessage(),
                        "Eingabe prüfen", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static Game applyValues(
            Game draft,
            JTextField titleField,
            JTextField developerField,
            JTextField releaseYearField,
            JTextField genresField,
            JTextField seriesField,
            JComboBox<GameCategories.Option> platformField,
            JComboBox<GameCategories.Option> distributionField,
            JComboBox<GameCategories.Option> storefrontField,
            JComboBox<GameCategories.Option> conditionField,
            JComboBox<GameCategories.Option> statusField,
            JTextField completionDateField,
            JTextField ratingField,
            JTextArea notesField) {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Bitte gib einen Titel ein.");
        }

        int releaseYear = parseInteger(releaseYearField.getText(), "Erscheinungsjahr");
        if (releaseYear < 0) {
            throw new IllegalArgumentException("Das Erscheinungsjahr darf nicht negativ sein.");
        }
        int rating = parseInteger(ratingField.getText(), "Bewertung");
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("Die Bewertung muss zwischen 0 und 10 liegen.");
        }

        LocalDate completionDate = null;
        String completionDateText = completionDateField.getText().trim();
        if (!completionDateText.isEmpty()) {
            try {
                completionDate = LocalDate.parse(completionDateText);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Das Abschlussdatum muss das Format JJJJ-MM-TT haben.");
            }
        }

        GameCategories.Option distributionType =
                (GameCategories.Option) distributionField.getSelectedItem();
        draft.setTitle(title);
        draft.setDeveloper(nullableText(developerField.getText()));
        draft.setReleaseYear(releaseYear);
        draft.setGenres(parseGenres(genresField.getText()));
        draft.setSeries(nullableText(seriesField.getText()));
        draft.setPlatform(optionId(platformField));
        draft.setDistributionType(optionId(distributionField));
        boolean digital = distributionType != null && "DIGITAL".equals(distributionType.getId());
        draft.setStorefront(digital ? optionId(storefrontField) : "NONE");
        draft.setCondition(digital ? "NOT_APPLICABLE" : optionId(conditionField));
        draft.setStatus(optionId(statusField));
        draft.setCompletionDate(completionDate);
        draft.setRating(rating);
        draft.setNotes(nullableText(notesField.getText()));
        return draft;
    }

    private static int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Bitte gib für „" + fieldName + "“ eine ganze Zahl ein.");
        }
    }

    private static List<String> parseGenres(String value) {
        List<String> genres = new ArrayList<>();
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(genre -> !genre.isEmpty())
                .forEach(genres::add);
        return genres;
    }

    private static Game copyOf(Game original) {
        Game copy = new Game();
        copy.setId(original.getId());
        copy.setTitle(original.getTitle());
        copy.setDeveloper(original.getDeveloper());
        copy.setReleaseYear(original.getReleaseYear());
        copy.setGenres(original.getGenres() == null
                ? new ArrayList<>() : new ArrayList<>(original.getGenres()));
        copy.setSeries(original.getSeries());
        copy.setPlatform(original.getPlatform());
        copy.setDistributionType(original.getDistributionType());
        copy.setStorefront(original.getStorefront());
        copy.setCondition(original.getCondition());
        copy.setStatus(original.getStatus());
        copy.setCompletionDate(original.getCompletionDate());
        copy.setRating(original.getRating());
        copy.setNotes(original.getNotes());
        return copy;
    }

    private static void addRow(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.insets = new Insets(5, 6, 5, 12);
        panel.add(new javax.swing.JLabel(label), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(5, 0, 5, 6);
        panel.add(field, fieldConstraints);
    }

    private static JComboBox<GameCategories.Option> createCombo(
            GameCategories categories, GameCategories.Category category) {
        return new JComboBox<>(categories.options(category).toArray(new GameCategories.Option[0]));
    }

    private static void selectOption(
            JComboBox<GameCategories.Option> combo,
            GameCategories categories,
            GameCategories.Category category,
            String id,
            String fallbackId) {
        GameCategories.Option selected = categories.find(category, id);
        if (selected == null) {
            selected = categories.find(category, fallbackId);
        }
        if (selected == null && combo.getItemCount() > 0) {
            selected = combo.getItemAt(0);
        }
        if (selected == null && id != null) {
            selected = new GameCategories.Option(id, id);
            combo.addItem(selected);
        }
        combo.setSelectedItem(selected);
    }

    private static String optionId(JComboBox<GameCategories.Option> combo) {
        GameCategories.Option option = (GameCategories.Option) combo.getSelectedItem();
        return option == null ? null : option.getId();
    }

    private static void updateDistributionFields(
            JComboBox<GameCategories.Option> distributionField,
            JComboBox<GameCategories.Option> storefrontField,
            JComboBox<GameCategories.Option> conditionField) {
        GameCategories.Option selected =
                (GameCategories.Option) distributionField.getSelectedItem();
        boolean digital = selected != null && "DIGITAL".equals(selected.getId());
        storefrontField.setEnabled(digital);
        conditionField.setEnabled(!digital);
    }

    private static String textOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String nullableText(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
