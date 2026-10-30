import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        String filePath = "spielebibliothek.json";
        File file = new File(filePath);

        if (!file.exists()) {
            showMessage("Die Datei '" + filePath + "' wurde nicht gefunden.\n"
                    + "Stelle sicher, dass sie im Projektordner liegt.",
                    "Datei nicht gefunden", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GameJsonStorage storage = new GameJsonStorage(filePath);

        try {
            List<Game> games = storage.load();
            if (games.isEmpty()) {
                showMessage("Die Bibliothek ist leer (keine Spiele gefunden).",
                        "Leere Spielebibliothek", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            SwingUtilities.invokeLater(() -> new GameLibraryFrame(games).setVisible(true));
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + e.getMessage());
            e.printStackTrace();
            showMessage("Fehler beim Lesen der Datei:\n" + e.getMessage(),
                    "Lesefehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showMessage(String message, String title, int messageType) {
        SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(null, message, title, messageType));
    }
}
