import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    //Gespeicherten Spieldaten laden und GUI
    public static void main(String[] args) {
        // Deklaration Liste fuer Spieleobjekte
        List<Game> games;

        try {
            // Lade die Spiele aus der deklarierten Datei
            games = GameLibraryStorage.load();
        } catch (IOException | IllegalArgumentException | DateTimeParseException e) {
            // Errorhändling, Dateizugriff, UngültigeDaten, Date+Time Invalide
            // Dann Ausgabe von Fehlermeldung
            JOptionPane.showMessageDialog(
                    null,
                    "Die Spiele konnten nicht geladen werden:\n" + e.getMessage(),
                    "Fehler beim Laden",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // GUI muss thread-sicher gestartet werden
        SwingUtilities.invokeLater(() -> {
            // Erstellt Hauptfenster und macht es sichtbar
            new GameLibraryFrame(games).setVisible(true);
        });
    }
}
