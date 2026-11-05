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

// Hauptfenster als JFrame  
public class GameLibraryFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    // Farbdefinitionen als Konstante
    private static final Color DARK = new Color(25, 32, 45);
    private static final Color LIGHT = new Color(245, 247, 250);

    // Laden der Komponenten und Daten
    private final List<Game> games;
    private final JList<Game> gameList;
    private final JTextArea details = new JTextArea();
    private final JButton editButton = new JButton("Bearbeiten");
    private final JLabel heading = new JLabel();

    // Lädt das Hauptfenster und mit @param games die Liste der Spiele
    public GameLibraryFrame(List<Game> games) {
        super("Spielebibliothek");
        this.games = new ArrayList<>(games);

        // Fensterdefaultwerte
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null); // Zentriert das Fenster auf dem Bildschirm

        // Sidebar für Navigation und Verwaltung
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(DARK);
        sidebar.setPreferredSize(new Dimension(260, 0));

        // Titelüberschrift mit Anzahl der geladenen Spiele
        updateHeading();
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setBorder(new EmptyBorder(20, 10, 20, 10));
        sidebar.add(heading, BorderLayout.NORTH);

        // Listenansicht für die Spiele
        gameList = new JList<>(games.toArray(new Game[0]));
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setBackground(DARK);
        gameList.setForeground(Color.WHITE);
        gameList.setSelectionBackground(new Color(65, 82, 115));
        gameList.setSelectionForeground(Color.WHITE);
        gameList.setFont(new Font("SansSerif", Font.PLAIN, 15));
        gameList.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Listener: Aktualisiert die Detailansicht bei Auswahl eines Eintrags
        gameList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedGame();
            }
        });

        // ScrollPane für die Spielliste
        JScrollPane gameListScrollPane = new JScrollPane(gameList);
        gameListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        gameListScrollPane.getViewport().setBackground(DARK);
        sidebar.add(gameListScrollPane, BorderLayout.CENTER);

        // Definition der Buttons
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setBorder(new EmptyBorder(10, 10, 14, 10));
        JButton addButton = new JButton("Hinzufügen");
        buttons.add(addButton);
        buttons.add(editButton);
        sidebar.add(buttons, BorderLayout.SOUTH);

        // Event-Handler für die Aktions-Buttons
        addButton.addActionListener(event -> addGame());
        editButton.addActionListener(event -> editGame());
        editButton.setEnabled(false); // Erst aktiv, wenn ein Spiel markiert ist

        // Rechter Bereich: Anzeige des ausgewählten Spiels
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

        // Hinzufügen der Hauptbereiche zum Fenster (BorderLayout)
        add(sidebar, BorderLayout.WEST);
        add(detailsScrollPane, BorderLayout.CENTER);
    }

    // Aktualisiert die Spieleanzahl beim Start/Hinzufügen/Löschen
    private void updateHeading() {
        heading.setText("  Meine Spiele (" + games.size() + ")");
    }

    // Hinzufügen eines neuen Spiels
    private void addGame() {
        Game game = showGameForm(null);
        if (game == null) {
            return;
        }

        // Kopie der Liste erstellen und persistent speichern
        List<Game> changedGames = new ArrayList<>(games);
        changedGames.add(game);
        if (saveGames(changedGames)) {
            games.add(game);
            gameList.setListData(games.toArray(new Game[0]));
            gameList.setSelectedIndex(games.size() - 1);
            updateHeading();
        }
    }

    // Ausgewähltes Spiel bearbeiten
    private void editGame() {
        int index = gameList.getSelectedIndex();
        if (index < 0) {
            return;
        }

        Game changedGame = showGameForm(games.get(index));
        if (changedGame == null) {
            return;
        }

        // Kopie der Liste mit modifiziertem Eintrag speichern
        List<Game> changedGames = new ArrayList<>(games);
        changedGames.set(index, changedGame);
        if (saveGames(changedGames)) {
            games.set(index, changedGame);
            gameList.setListData(games.toArray(new Game[0]));
            gameList.setSelectedIndex(index);
        }
    }

    // Über Spiel bearbeiten ist es möglich, Spiel zu löschen
    private void deleteGame() {
        int index = gameList.getSelectedIndex();
        if (index < 0) {
            return;
        }

        // Sicherheitsabfrage, ob wirklich gelöscht werden soll
        Game toDelete = games.get(index);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Möchtest du das Spiel \"" + toDelete.getTitle() + "\" wirklich löschen?",
                "Spiel löschen",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        List<Game> changedGames = new ArrayList<>(games);
        changedGames.remove(index);

        if (saveGames(changedGames)) {
            games.remove(index);
            gameList.setListData(games.toArray(new Game[0]));
            gameList.clearSelection();
            showSelectedGame(); // Setzt Details und Button-Status zurueck
            updateHeading();
        }
    }

    
     // Zeigt das Eingabeformular (JOptionPane) an
     // Nur das Feld "Titel" ist verpflichtend; alle anderen Felder sind optional
     // Beim Bearbeiten steht zusätzlich die Option "Spiel löschen" zur Verfügung
    private Game showGameForm(Game oldGame) {
        JTextField title = new JTextField(oldGame == null ? "" : oldGame.getTitle());
        JTextField developer = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getDeveloper()));
        JTextField year = new JTextField(oldGame == null || oldGame.getReleaseYear() <= 0
                ? "" : String.valueOf(oldGame.getReleaseYear()));
        JTextField series = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getSeries()));
        JTextField genres = new JTextField(oldGame == null
                ? "" : String.join(", ", oldGame.getGenres()));
        JTextField date = new JTextField(oldGame == null || oldGame.getCompletionDate() == null
                ? "" : oldGame.getCompletionDate().toString());
        JTextField rating = new JTextField(oldGame == null || oldGame.getRating() <= 0
                ? "" : String.valueOf(oldGame.getRating()));
        JTextField platform = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getPlatform()));
        JComboBox<DistributionType> type = new JComboBox<>(DistributionType.values());
        JTextField store = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getStorefront()));
        JTextField condition = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getCondition()));
        JTextField status = new JTextField(oldGame == null ? "" : textOrEmpty(oldGame.getStatus()));
        JTextArea notes = new JTextArea(oldGame == null ? "" : textOrEmpty(oldGame.getNotes()), 3, 20);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);

        // Vorbelegung für Dropdown-Feld
        if (oldGame != null && oldGame.getDistributionType() != null) {
            type.setSelectedItem(oldGame.getDistributionType());
        }

        // Layout fürs Formular
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
        addField(form, "Titel (Pflicht)", title);
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

        int answer;
        String dialogTitle = oldGame == null ? "Spiel hinzufügen" : "Spiel bearbeiten";

        //Optionen im Diaglog
        if (oldGame == null) {
            // Beim Hinzufügen: Nur OK und Abbrechen
            answer = JOptionPane.showConfirmDialog(
                    this, form, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return null;
            }
        } else {
            // Beim Bearbeiten: Speichern , Spiel löschen, Abbrechen
            Object[] options = {"Speichern", "Spiel löschen", "Abbrechen"};
            answer = JOptionPane.showOptionDialog(
                    this, form, dialogTitle, JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (answer == 1) { // Button "Spiel löschen" ausgewählt
                deleteGame();
                return null;
            }
            if (answer != 0) { // Abgebrochen oder Dialog geschlossen
                return null;
            }
        }

        try {
            // Prüfung: Titel leer? Ansonsten Fehlermeldung
            if (title.getText().isBlank()) {
                throw new IllegalArgumentException("Bitte gib mindestens einen Titel ein.");
            }

            // Prüfung Jahr, ansonsten "0" ,darf nicht NULL sein
            int releaseYear = 0;
            String yearInput = year.getText().trim();
            if (!yearInput.isEmpty()) {
                releaseYear = Integer.parseInt(yearInput);
            }

            // Prüfung Bewertung, ansonsten "0", darf nicht NULL sein
            int gameRating = 0;
            String ratingInput = rating.getText().trim();
            if (!ratingInput.isEmpty()) {
                gameRating = Integer.parseInt(ratingInput);
                if (gameRating < 0 || gameRating > 10) {
                    throw new IllegalArgumentException("Die Bewertung muss zwischen 0 und 10 liegen.");
                }
            }

            // Ermitteln des Distributionstyps (Fallback auf ersten Enum-Wert)
            DistributionType distType = (DistributionType) type.getSelectedItem();
            if (distType == null && DistributionType.values().length > 0) {
                distType = DistributionType.values()[0];
            }

            // Neues Game-Objekt mit Basisdaten erzeugen
            Game game = new Game(
                    title.getText().trim(),
                    platform.getText().trim(),
                    distType,
                    releaseYear);

            // Bestehende ID beibehalten, falls es sich um ein Update handelt
            if (oldGame != null) {
                game.setId(oldGame.getId());
            }

            // Optionale Textfelder setzen (leere Eingaben werden zu null konvertiert)
            game.setDeveloper(emptyOrNull(developer.getText()));
            game.setSeries(emptyOrNull(series.getText()));
            game.setStorefront(emptyOrNull(store.getText()));
            game.setCondition(emptyOrNull(condition.getText()));
            game.setStatus(emptyOrNull(status.getText()));
            game.setRating(gameRating);
            game.setNotes(emptyOrNull(notes.getText()));

            // Parsen des optionalen Datums (JJJJ-MM-TT)
            String enteredDate = date.getText().trim();
            if (!enteredDate.isEmpty()) {
                game.setCompletionDate(LocalDate.parse(enteredDate));
            } else {
                game.setCompletionDate(null);
            }

            // Parsen der kommagetrennten Genre-Liste
            if (!genres.getText().isBlank()) {
                for (String genre : genres.getText().split(",")) {
                    if (!genre.isBlank()) {
                        game.getGenres().add(genre.trim());
                    }
                }
            }

            return game;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Erscheinungsjahr und Bewertung müssen ganze Zahlen sein.",
                    "Ungültige Zahleneingabe", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Das Datum muss im Format JJJJ-MM-TT vorliegen (oder leer bleiben).",
                    "Ungültiges Datumsformat", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Ungültige Eingabe", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Hilfsmethode: Hinzufügen Beschriftungs- und Eingabefeld-Paars
    private void addField(JPanel form, String label, java.awt.Component input) {
        form.add(new JLabel(label));
        form.add(input);
    }

    // Speichert die Liste über die Speicherklasse.
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

    // Reagiert auf Änderungen in der Listen-Auswahl und aktualisiert den Details
    private void showSelectedGame() {
        int index = gameList.getSelectedIndex();
        editButton.setEnabled(index >= 0);
        if (index >= 0) {
            showGameDetails(games.get(index));
        } else {
            details.setText("Wähle links ein Spiel aus, um die Details zu sehen.");
        }
    }

    // Formatiert alle Daten des ausgewählten Spiels als Text und stellt sie dar.

    private void showGameDetails(Game game) {
        StringBuilder text = new StringBuilder();
        text.append(game.getTitle());
        // Erscheinungsjahr nur anzeigen, wenn es größer als 0 ist
        if (game.getReleaseYear() > 0) {
            text.append(" (").append(game.getReleaseYear()).append(")");
        }
        text.append("\n========================================\n\n");
        text.append("Entwickler: ").append(valueOrDash(game.getDeveloper())).append('\n');
        text.append("Reihe: ").append(valueOrDash(game.getSeries())).append('\n');
        text.append("Plattform: ").append(valueOrDash(game.getPlatform())).append('\n');
        text.append("Format: ").append(valueOrDash(game.getDistributionType())).append('\n');

        // Dynamische Anzeige je nach Vertriebsart (Digital vs. Physisch)
        if (game.getDistributionType() == DistributionType.DIGITAL) {
            text.append("Store: ").append(valueOrDash(game.getStorefront())).append('\n');
        } else {
            text.append("Zustand: ").append(valueOrDash(game.getCondition())).append('\n');
        }

        text.append("Status: ").append(valueOrDash(game.getStatus())).append('\n');
        text.append("Beendet am: ").append(valueOrDash(game.getCompletionDate())).append('\n');
        text.append("Bewertung: ");
        // Bewertung nur anzeigen, wenn es ungleich 0
        if (game.getRating() > 0) {
            text.append(game.getRating()).append(" / 10\n");
        } else {
            text.append("Keine\n");
        }

        text.append("Genres: ");
        if (game.getGenres() == null || game.getGenres().isEmpty()) {
            text.append("-\n");
        } else {
            text.append(String.join(", ", game.getGenres())).append('\n');
        }

        if (game.getNotes() != null && !game.getNotes().isBlank()) {
            text.append("\nNotizen:\n").append(game.getNotes());
        }

        details.setText(text.toString());
        details.setCaretPosition(0); // Scrollt im Textbereich nach ganz oben
    }

    //Gibt den Text des Objekts zurück oder einen Bindestrich "-", wenn der Wert leer/null ist
    private String valueOrDash(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }

    //Gibt den übergebenen String zurück oder einen leeren String "", falls der Wert null ist
    private String textOrEmpty(String value) {
        return value == null ? "" : value;
    }

    //Bereinigt Leerzeichen und konvertiert leere Strings in {@code null},
    //um unnötige Leerstrings in Datenobjekten zu vermeiden.
    private String emptyOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
