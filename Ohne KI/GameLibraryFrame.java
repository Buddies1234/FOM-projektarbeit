import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class GameLibraryFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color DARK = new Color(25, 32, 45);
    private static final Color LIGHT = new Color(245, 247, 250);

    private final List<Game> games;
    private final JList<Game> gameList;
    private final JTextArea details = new JTextArea();
    private final JButton editButton = new JButton("Bearbeiten");

    public GameLibraryFrame(List<Game> games) {
        super("Spielebibliothek");
        this.games = new ArrayList<>(games);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(DARK);
        sidebar.setPreferredSize(new Dimension(260, 0));

        JLabel heading = new JLabel("  Meine Spiele (" + games.size() + ")");
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setBorder(new EmptyBorder(20, 10, 20, 10));
        sidebar.add(heading, BorderLayout.NORTH);

        gameList = new JList<>(games.toArray(new Game[0]));
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setBackground(DARK);
        gameList.setForeground(Color.WHITE);
        gameList.setSelectionBackground(new Color(65, 82, 115));
        gameList.setSelectionForeground(Color.WHITE);
        gameList.setFont(new Font("SansSerif", Font.PLAIN, 15));
        gameList.setBorder(new EmptyBorder(5, 10, 5, 10));
        gameList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedGame();
            }
        });

        JScrollPane gameListScrollPane = new JScrollPane(gameList);
        gameListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        gameListScrollPane.getViewport().setBackground(DARK);
        sidebar.add(gameListScrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setBorder(new EmptyBorder(10, 10, 14, 10));
        JButton addButton = new JButton("Hinzufügen");
        buttons.add(addButton);
        buttons.add(editButton);
        sidebar.add(buttons, BorderLayout.SOUTH);

        addButton.addActionListener(event -> addGame());
        editButton.addActionListener(event -> editGame());
        editButton.setEnabled(false);

        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new Font("SansSerif", Font.PLAIN, 16));
        details.setBackground(LIGHT);
        details.setForeground(new Color(35, 44, 58));
        details.setBorder(new EmptyBorder(25, 28, 25, 28));
        details.setText("Wähle links ein Spiel aus, um die Details zu sehen.");

        JScrollPane detailsScrollPane = new JScrollPane(details);
        detailsScrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(sidebar, BorderLayout.WEST);
        add(detailsScrollPane, BorderLayout.CENTER);
    }

    private void addGame() {
        Game game = showGameForm(null);
        if (game == null) {
            return;
        }

        List<Game> changedGames = new ArrayList<>(games);
        changedGames.add(game);
        if (saveGames(changedGames)) {
            games.add(game);
            gameList.setListData(games.toArray(new Game[0]));
            gameList.setSelectedIndex(games.size() - 1);
        }
    }

    private void editGame() {
        int index = gameList.getSelectedIndex();
        if (index < 0) {
            return;
        }

        Game changedGame = showGameForm(games.get(index));
        if (changedGame == null) {
            return;
        }

        List<Game> changedGames = new ArrayList<>(games);
        changedGames.set(index, changedGame);
        if (saveGames(changedGames)) {
            games.set(index, changedGame);
            gameList.setListData(games.toArray(new Game[0]));
            gameList.setSelectedIndex(index);
        }
    }

    private Game showGameForm(Game oldGame) {
        JTextField title = new JTextField(oldGame == null ? "" : oldGame.getTitle());
        JTextField developer = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getDeveloper()));
        JTextField year = new JTextField(oldGame == null ? "" : String.valueOf(oldGame.getReleaseYear()));
        JTextField series = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getSeries()));
        JTextField genres = new JTextField(oldGame == null
                ? "" : String.join(", ", oldGame.getGenres()));
        JTextField date = new JTextField(oldGame == null || oldGame.getCompletionDate() == null
                ? "" : oldGame.getCompletionDate().toString());
        JTextField rating = new JTextField(oldGame == null ? "0" : String.valueOf(oldGame.getRating()));
        JTextField platform = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getPlatform()));
        JComboBox<DistributionType> type = new JComboBox<>(DistributionType.values());
        JTextField store = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getStorefront()));
        JTextField condition = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getCondition()));
        JTextField status = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getStatus()));
        JTextArea notes = new JTextArea(oldGame == null ? "" : textOrEmpty(oldGame.getNotes()), 3, 20);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);

        if (oldGame != null) {
            type.setSelectedItem(oldGame.getDistributionType());
        } else {
            condition.setText("GOOD");
            store.setText("NONE");
            status.setText("UNPLAYED");
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        addField(form, "Titel", title);
        addField(form, "Entwickler", developer);
        addField(form, "Erscheinungsjahr", year);
        addField(form, "Plattform", platform);
        addField(form, "Format", type);
        addField(form, "Store", store);
        addField(form, "Zustand", condition);
        addField(form, "Spielstatus", status);
        addField(form, "Bewertung (0-10)", rating);
        addField(form, "Beendet am (JJJJ-MM-TT)", date);
        addField(form, "Genres (mit Komma trennen)", genres);
        addField(form, "Spielreihe", series);
        addField(form, "Notizen", new JScrollPane(notes));

        String dialogTitle = oldGame == null ? "Spiel hinzufügen" : "Spiel bearbeiten";
        int answer = JOptionPane.showConfirmDialog(
                this, form, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return null;
        }

        try {
            if (title.getText().isBlank()) {
                throw new IllegalArgumentException("Bitte gib einen Titel ein.");
            }

            int releaseYear = Integer.parseInt(year.getText().trim());
            int gameRating = Integer.parseInt(rating.getText().trim());
            if (gameRating < 0 || gameRating > 10) {
                throw new IllegalArgumentException("Die Bewertung muss zwischen 0 und 10 liegen.");
            }

            Game game = new Game(
                    title.getText().trim(),
                    platform.getText().trim(),
                    (DistributionType) type.getSelectedItem(),
                    releaseYear);
            if (oldGame != null) {
                game.setId(oldGame.getId());
            }
            game.setDeveloper(emptyOrNull(developer.getText()));
            game.setSeries(emptyOrNull(series.getText()));
            game.setStorefront(emptyOrNull(store.getText()));
            game.setCondition(emptyOrNull(condition.getText()));
            game.setStatus(emptyOrNull(status.getText()));
            game.setRating(gameRating);
            game.setNotes(emptyOrNull(notes.getText()));

            String enteredDate = date.getText().trim();
            if (!enteredDate.isEmpty()) {
                game.setCompletionDate(LocalDate.parse(enteredDate));
            }

            for (String genre : genres.getText().split(",")) {
                if (!genre.isBlank()) {
                    game.getGenres().add(genre.trim());
                }
            }
            return game;
        } catch (NumberFormatException | DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Bitte prüfe Jahr, Bewertung und Datum.\nDas Datum muss JJJJ-MM-TT sein.",
                    "Ungültige Eingabe", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Ungültige Eingabe", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void addField(JPanel form, String label, java.awt.Component input) {
        form.add(new JLabel(label));
        form.add(input);
    }

    private boolean saveGames(List<Game> changedGames) {
        try {
            GameLibraryStorage.save(changedGames);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Die Änderungen konnten nicht gespeichert werden:\n" + e.getMessage(),
                    "Fehler beim Speichern", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void showSelectedGame() {
        int index = gameList.getSelectedIndex();
        editButton.setEnabled(index >= 0);
        if (index >= 0) {
            showGameDetails(games.get(index));
        } else {
            details.setText("Wähle links ein Spiel aus, um die Details zu sehen.");
        }
    }

    private void showGameDetails(Game game) {
        StringBuilder text = new StringBuilder();
        text.append(game.getTitle()).append(" (").append(game.getReleaseYear()).append(")\n");
        text.append("========================================\n\n");
        text.append("Entwickler: ").append(valueOrDash(game.getDeveloper())).append('\n');
        text.append("Reihe: ").append(valueOrDash(game.getSeries())).append('\n');
        text.append("Plattform: ").append(valueOrDash(game.getPlatform())).append('\n');
        text.append("Format: ").append(valueOrDash(game.getDistributionType())).append('\n');

        if (game.getDistributionType() == DistributionType.DIGITAL) {
            text.append("Store: ").append(valueOrDash(game.getStorefront())).append('\n');
        } else {
            text.append("Zustand: ").append(valueOrDash(game.getCondition())).append('\n');
        }

        text.append("Status: ").append(valueOrDash(game.getStatus())).append('\n');
        text.append("Beendet am: ").append(valueOrDash(game.getCompletionDate())).append('\n');
        text.append("Bewertung: ");
        if (game.getRating() > 0) {
            text.append(game.getRating()).append(" / 10\n");
        } else {
            text.append("Keine\n");
        }

        text.append("Genres: ");
        if (game.getGenres().isEmpty()) {
            text.append("-\n");
        } else {
            text.append(String.join(", ", game.getGenres())).append('\n');
        }

        if (game.getNotes() != null && !game.getNotes().isBlank()) {
            text.append("\nNotizen:\n").append(game.getNotes());
        }

        details.setText(text.toString());
        details.setCaretPosition(0);
    }

    private String valueOrDash(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }

    private String textOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyOrNull(String value) {
        return value.isBlank() ? null : value.trim();
    }
}
