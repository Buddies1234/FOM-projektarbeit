import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.text.Collator;
import java.util.Locale;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GameLibraryFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private enum SortField {
        TITLE("Titel"),
        RELEASE_YEAR("Erscheinungsjahr"),
        PLATFORM("Plattform");

        private final String label;

        SortField(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color SIDEBAR = new Color(25, 32, 45);
    private static final Color SIDEBAR_MUTED = new Color(170, 181, 198);
    private static final Color ACCENT = new Color(93, 120, 255);
    private static final Color TEXT = new Color(35, 44, 58);
    private static final Color MUTED = new Color(112, 122, 138);
    private static final Color CARD = Color.WHITE;

    private final JPanel detailsPanel = new JPanel(new BorderLayout());
    private final transient GameJsonStorage storage;
    private final transient GameCategoryStorage categoryStorage;
    private final transient List<Game> games;
    private final DefaultListModel<Game> gamesModel = new DefaultListModel<>();
    private final JList<Game> gameList = new JList<>(gamesModel);
    private transient GameCategories categories;
    private boolean adminMode;
    private JLabel gameCountLabel;
    private JButton editButton;
    private JButton deleteButton;
    private JButton adminModeButton;
    private JButton categoriesButton;
    private JComboBox<SortField> sortFieldSelector;
    private JButton sortDirectionButton;
    private JTextField searchField;
    private boolean sortAscending = true;

    public GameLibraryFrame(List<Game> games) {
        this(games, new GameJsonStorage("spielebibliothek.json"));
    }

    public GameLibraryFrame(List<Game> games, GameJsonStorage storage) {
        this(games, storage, new GameCategories(),
                new GameCategoryStorage("spielekategorien.properties"));
    }

    public GameLibraryFrame(
            List<Game> games,
            GameJsonStorage storage,
            GameCategories categories,
            GameCategoryStorage categoryStorage) {
        super("Spielebibliothek");
        this.games = new ArrayList<>(games);
        this.storage = storage;
        this.categories = new GameCategories(categories);
        this.categoryStorage = categoryStorage;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        for (Game game : this.games) {
            gamesModel.addElement(game);
        }

        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setCellRenderer(new GameListCellRenderer());
        gameList.setBackground(SIDEBAR);
        gameList.setBorder(new EmptyBorder(8, 10, 12, 10));
        gameList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                Game selectedGame = gameList.getSelectedValue();
                if (selectedGame == null) {
                    showSelectionPrompt();
                } else {
                    showGameDetails(selectedGame);
                }
                updateActionButtons();
            }
        });

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(290, 0));
        sidebar.add(createSidebarHeader(this.games.size()), BorderLayout.NORTH);
        sortGames();
        JScrollPane gameListScrollPane = new JScrollPane(gameList);
        gameListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        gameListScrollPane.getViewport().setBackground(SIDEBAR);
        sidebar.add(gameListScrollPane, BorderLayout.CENTER);
        sidebar.add(createSidebarActions(), BorderLayout.SOUTH);

        detailsPanel.setBackground(BACKGROUND);
        detailsPanel.setBorder(new EmptyBorder(30, 34, 30, 34));
        showSelectionPrompt();

        add(sidebar, BorderLayout.WEST);
        add(detailsPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebarHeader(int gameCount) {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setBackground(SIDEBAR);
        header.setBorder(new EmptyBorder(28, 22, 20, 18));

        JLabel title = new JLabel("Meine Spiele");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        gameCountLabel = new JLabel();
        gameCountLabel.setForeground(SIDEBAR_MUTED);
        gameCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        updateGameCount(gameCount);

        JPanel sortControls = new JPanel(new BorderLayout(6, 0));
        sortControls.setOpaque(false);
        searchField = new JTextField();
        searchField.setToolTipText("Suche nach Titel, Entwickler, Plattform und mehr");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 91, 112)),
                new EmptyBorder(5, 7, 5, 7)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                applyFilter();
            }
        });
        sortFieldSelector = new JComboBox<>(SortField.values());
        sortDirectionButton = new JButton("Aufsteigend");
        sortFieldSelector.addActionListener(event -> applySort());
        sortDirectionButton.addActionListener(event -> {
            sortAscending = !sortAscending;
            sortDirectionButton.setText(sortAscending ? "Aufsteigend" : "Absteigend");
            applySort();
        });
        JPanel sortOptions = new JPanel(new BorderLayout(0, 4));
        sortOptions.setOpaque(false);
        sortOptions.add(sortFieldSelector, BorderLayout.NORTH);
        sortOptions.add(sortDirectionButton, BorderLayout.SOUTH);
        sortControls.add(searchField, BorderLayout.NORTH);
        sortControls.add(sortOptions, BorderLayout.CENTER);

        header.add(title, BorderLayout.NORTH);
        JPanel headerContent = new JPanel(new BorderLayout(0, 8));
        headerContent.setOpaque(false);
        headerContent.add(gameCountLabel, BorderLayout.NORTH);
        headerContent.add(sortControls, BorderLayout.CENTER);
        header.add(headerContent, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createSidebarActions() {
        JPanel actions = new JPanel(new GridLayout(0, 2, 6, 6));
        actions.setBackground(SIDEBAR);
        actions.setBorder(new EmptyBorder(10, 12, 14, 12));

        JButton addButton = new JButton("Neu");
        editButton = new JButton("Bearbeiten");
        deleteButton = new JButton("Löschen");
        adminModeButton = new JButton("Admin-Modus: AUS");
        categoriesButton = new JButton("Kategorien");
        addButton.addActionListener(event -> addGame());
        editButton.addActionListener(event -> editSelectedGame());
        deleteButton.addActionListener(event -> deleteSelectedGame());
        adminModeButton.addActionListener(event -> toggleAdminMode());
        categoriesButton.addActionListener(event -> manageCategories());

        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(adminModeButton);
        actions.add(categoriesButton);
        updateActionButtons();
        return actions;
    }

    private void addGame() {
        Game draft = new Game("", "PC", "PHYSICAL", 0);
        draft.setStorefront("NONE");
        Game createdGame = GameEditorDialog.showDialog(this, draft, categories);
        if (createdGame == null) {
            return;
        }

        searchField.setText("");
        List<Game> updatedGames = new ArrayList<>(games);
        updatedGames.add(createdGame);
        saveAndRefresh(updatedGames, createdGame.getId(), -1);
    }

    private void editSelectedGame() {
        Game selectedGame = gameList.getSelectedValue();
        if (selectedGame == null) {
            return;
        }

        Game updatedGame = GameEditorDialog.showDialog(
                this, selectedGame, categories);
        if (updatedGame == null) {
            return;
        }

        List<Game> updatedGames = new ArrayList<>(games);
        int selectedIndex = indexOfGame(selectedGame.getId());
        updatedGames.set(selectedIndex, updatedGame);
        saveAndRefresh(updatedGames, updatedGame.getId(), gameList.getSelectedIndex());
    }

    private void deleteSelectedGame() {
        Game selectedGame = gameList.getSelectedValue();
        if (selectedGame == null) {
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Möchtest du „" + selectedGame.getTitle() + "“ wirklich löschen?",
                "Spiel löschen", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        List<Game> updatedGames = new ArrayList<>(games);
        updatedGames.remove(indexOfGame(selectedGame.getId()));
        saveAndRefresh(updatedGames, null, gameList.getSelectedIndex());
    }

    private void saveAndRefresh(List<Game> updatedGames, String selectedId, int fallbackIndex) {
        try {
            storage.save(updatedGames);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Die Änderungen konnten nicht gespeichert werden:\n" + e.getMessage(),
                    "Speicherfehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        games.clear();
        games.addAll(updatedGames);
        updateGameCount(games.size());
        sortGames();

        if (selectedId != null) {
            for (int i = 0; i < gamesModel.size(); i++) {
                if (selectedId.equals(gamesModel.get(i).getId())) {
                    gameList.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (gameList.getSelectedIndex() < 0 && !gamesModel.isEmpty()) {
            gameList.setSelectedIndex(Math.min(
                    Math.max(fallbackIndex, 0), gamesModel.size() - 1));
        } else {
            if (gamesModel.isEmpty()) {
                showSelectionPrompt();
            }
        }
        updateActionButtons();
    }

    private void applySort() {
        String selectedId = gameList.getSelectedValue() == null
                ? null : gameList.getSelectedValue().getId();
        sortGames();
        if (selectedId != null) {
            for (int i = 0; i < games.size(); i++) {
                if (selectedId.equals(games.get(i).getId())) {
                    gameList.setSelectedIndex(i);
                    return;
                }
            }
        } else {
            showSelectionPrompt();
        }
    }

    private void sortGames() {
        SortField selectedField = (SortField) sortFieldSelector.getSelectedItem();
        if (selectedField == null) {
            return;
        }

        Comparator<Game> comparator;
        switch (selectedField) {
            case RELEASE_YEAR:
                comparator = Comparator.comparingInt(Game::getReleaseYear);
                break;
            case PLATFORM:
                comparator = textComparator(game ->
                        categoryLabel(GameCategories.Category.PLATFORM, game.getPlatform()));
                break;
            case TITLE:
            default:
                comparator = textComparator(Game::getTitle);
                break;
        }
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        games.sort(comparator.thenComparing(
                textComparator(Game::getTitle)).thenComparing(Game::getId));
        applyFilter();
    }

    private void applyFilter() {
        Game selectedGame = gameList.getSelectedValue();
        String selectedId = selectedGame == null ? null : selectedGame.getId();
        String query = searchField == null ? "" : searchField.getText().trim();
        gamesModel.clear();
        for (Game game : GameSearch.filter(games, query, categories)) {
            gamesModel.addElement(game);
        }
        if (selectedId != null) {
            for (int i = 0; i < gamesModel.size(); i++) {
                if (selectedId.equals(gamesModel.get(i).getId())) {
                    gameList.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (gameList.getSelectedIndex() < 0) {
            if (!gamesModel.isEmpty()) {
                showSelectionPrompt();
            } else {
                showNoSearchResults(query);
            }
        }
        updateActionButtons();
    }

    private int indexOfGame(String id) {
        for (int i = 0; i < games.size(); i++) {
            if (id.equals(games.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private void showNoSearchResults(String query) {
        JPanel prompt = new JPanel(new BorderLayout(0, 10));
        prompt.setOpaque(false);

        JLabel title = new JLabel("Keine Spiele gefunden");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Keine Treffer für „" + query + "“.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));

        prompt.add(title, BorderLayout.NORTH);
        prompt.add(subtitle, BorderLayout.CENTER);
        detailsPanel.removeAll();
        detailsPanel.add(prompt, BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private Comparator<Game> textComparator(Function<Game, String> valueGetter) {
        Collator collator = Collator.getInstance(Locale.GERMAN);
        collator.setStrength(Collator.PRIMARY);
        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        return (first, second) -> collator.compare(
                valueGetter.apply(first) == null ? "" : valueGetter.apply(first),
                valueGetter.apply(second) == null ? "" : valueGetter.apply(second));
    }

    private void updateGameCount(int gameCount) {
        if (gameCountLabel != null) {
            gameCountLabel.setText(gameCount + (gameCount == 1 ? " Spiel" : " Spiele"));
        }
    }

    private void updateActionButtons() {
        boolean hasSelection = gameList.getSelectedIndex() >= 0;
        if (editButton != null) {
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        }
        if (categoriesButton != null) {
            categoriesButton.setEnabled(adminMode);
        }
    }

    private void toggleAdminMode() {
        adminMode = !adminMode;
        adminModeButton.setText(adminMode ? "Admin-Modus: AN" : "Admin-Modus: AUS");
        updateActionButtons();
    }

    private void manageCategories() {
        if (!adminMode) {
            return;
        }
        GameCategories updatedCategories =
                CategoryAdminDialog.showDialog(this, categories, games);
        if (updatedCategories == null) {
            return;
        }
        try {
            categoryStorage.save(updatedCategories);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Die Kategorien konnten nicht gespeichert werden:\n" + e.getMessage(),
                    "Speicherfehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        categories = updatedCategories;
        gameList.repaint();
        Game selectedGame = gameList.getSelectedValue();
        if (selectedGame == null) {
            showSelectionPrompt();
        } else {
            showGameDetails(selectedGame);
        }
    }

    private void showSelectionPrompt() {
        JPanel prompt = new JPanel(new BorderLayout(0, 10));
        prompt.setOpaque(false);

        JLabel title = new JLabel("Wähle ein Spiel aus");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Die Details werden hier angezeigt.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JPanel message = new JPanel(new BorderLayout(0, 8));
        message.setOpaque(false);
        message.add(title, BorderLayout.NORTH);
        message.add(subtitle, BorderLayout.CENTER);
        prompt.add(message, BorderLayout.CENTER);

        detailsPanel.removeAll();
        detailsPanel.add(prompt, BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void showGameDetails(Game game) {
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setOpaque(false);

        JPanel heading = new JPanel(new BorderLayout(0, 8));
        heading.setOpaque(false);

        JLabel title = new JLabel(game.getTitle() + " (" + game.getReleaseYear() + ")");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));

        JLabel subtitle = new JLabel(valueOrDash(game.getDeveloper())
                + "  ·  " + categoryLabel(GameCategories.Category.PLATFORM,
                        game.getPlatform()));
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));

        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.CENTER);

        JPanel sections = new JPanel(new GridBagLayout());
        sections.setOpaque(false);
        ((GridBagLayout) sections.getLayout()).columnWeights = new double[] {1, 1};
        sections.setBorder(new EmptyBorder(2, 0, 0, 0));

        int row = 0;
        addDetailCard(sections, "SPIEL", row, 0,
                detail("Entwickler", valueOrDash(game.getDeveloper())),
                detail("Reihe", valueOrDash(game.getSeries())),
                detail("Genres", game.getGenres() == null || game.getGenres().isEmpty()
                        ? "-" : String.join(", ", game.getGenres())));

        boolean digital = "DIGITAL".equals(game.getDistributionType());
        String physicalOrDigital = categoryLabel(
                GameCategories.Category.DISTRIBUTION_TYPE, game.getDistributionType());
        String distributionDetail = digital
                ? categoryLabel(GameCategories.Category.STOREFRONT, game.getStorefront())
                : categoryLabel(GameCategories.Category.CONDITION, game.getCondition());
        addDetailCard(sections, "AUSGABE", row++, 1,
                detail("Plattform", categoryLabel(GameCategories.Category.PLATFORM,
                        game.getPlatform())),
                detail("Format", physicalOrDigital),
                detail(digital ? "Store" : "Zustand",
                        distributionDetail));

        addDetailCard(sections, "FORTSCHRITT", row, 0,
                detail("Status", categoryLabel(GameCategories.Category.PLAY_STATUS,
                        game.getStatus())),
                detail("Beendet am", game.getCompletionDate() == null
                        ? "-" : game.getCompletionDate().toString()),
                detail("Bewertung", game.getRating() > 0 ? game.getRating() + " / 10" : "Keine"));

        addDetailCard(sections, "BIBLIOTHEK", row++, 1,
                detail("Spiel-ID", valueOrDash(game.getId())));

        JPanel allContent = new JPanel(new BorderLayout(0, 22));
        allContent.setOpaque(false);
        allContent.add(heading, BorderLayout.NORTH);
        allContent.add(sections, BorderLayout.CENTER);

        if (game.getNotes() != null && !game.getNotes().isBlank()) {
            allContent.add(createNotesCard(game.getNotes()), BorderLayout.SOUTH);
        }

        JScrollPane scrollPane = new JScrollPane(allContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        detailsPanel.removeAll();
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void addDetailCard(JPanel parent, String title, int row, int column, Detail... details) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(18, 19, 18, 19));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        JLabel cardTitle = new JLabel(title);
        cardTitle.setForeground(ACCENT);
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        constraints.insets = new Insets(0, 0, 14, 0);
        card.add(cardTitle, constraints);

        for (Detail detail : details) {
            constraints.gridy++;
            constraints.insets = new Insets(0, 0, 10, 0);
            card.add(createDetailRow(detail), constraints);
        }

        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.gridx = column;
        cardConstraints.gridy = row;
        cardConstraints.weightx = 1;
        cardConstraints.fill = GridBagConstraints.HORIZONTAL;
        cardConstraints.anchor = GridBagConstraints.NORTHWEST;
        cardConstraints.insets = new Insets(0, column == 0 ? 0 : 10, 12, column == 0 ? 10 : 0);
        parent.add(card, cardConstraints);
    }

    private JPanel createDetailRow(Detail detail) {
        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setOpaque(false);

        JLabel label = new JLabel(detail.label);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel value = new JLabel(detail.value);
        value.setForeground(TEXT);
        value.setFont(new Font("SansSerif", Font.PLAIN, 14));

        row.add(label, BorderLayout.NORTH);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JPanel createNotesCard(String notes) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(18, 19, 18, 19));

        JLabel title = new JLabel("NOTIZEN");
        title.setForeground(ACCENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));

        JTextArea text = new JTextArea(notes);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setForeground(TEXT);
        text.setFont(new Font("SansSerif", Font.PLAIN, 14));
        text.setBorder(null);

        card.add(title, BorderLayout.NORTH);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private Detail detail(String label, String value) {
        return new Detail(label, value);
    }

    private String valueOrDash(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }

    private String categoryLabel(GameCategories.Category category, String id) {
        return categories.label(category, id);
    }

    private static class Detail {
        private final String label;
        private final String value;

        private Detail(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    private class GameListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            Game game = (Game) value;
            setText("<html><div style='padding: 3px 0;'>"
                    + "<b>" + escapeHtml(valueOrDash(game.getTitle())) + "</b><br>"
                    + "<span style='color:#aab5c6;'>"
                    + escapeHtml(categoryLabel(GameCategories.Category.PLATFORM,
                            game.getPlatform())) + " · "
                    + game.getReleaseYear() + "</span></div></html>");
            setFont(new Font("SansSerif", Font.PLAIN, 14));
            setOpaque(true);
            setBorder(new EmptyBorder(10, 12, 10, 10));
            setBackground(isSelected ? new Color(52, 65, 91) : SIDEBAR);
            setForeground(Color.WHITE);
            return this;
        }

        private String valueOrDash(Object value) {
            return value == null || value.toString().isBlank() ? "-" : value.toString();
        }

        private String escapeHtml(String text) {
            return text.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }
    }
}
