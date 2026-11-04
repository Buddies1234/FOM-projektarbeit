import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        List<Game> games;
        try {
            games = GameLibraryStorage.load();
        } catch (IOException | IllegalArgumentException | DateTimeParseException e) {
            JOptionPane.showMessageDialog(null,
                    "Die Spiele konnten nicht geladen werden:\n" + e.getMessage(),
                    "Fehler beim Laden", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new GameLibraryFrame(games).setVisible(true));
    }
}
