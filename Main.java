import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        String filePath = "spielebibliothek.json";
        GameJsonStorage storage = new GameJsonStorage(filePath);
        GameCategoryStorage categoryStorage =
                new GameCategoryStorage("spielekategorien.properties");

        try {
            List<Game> games = storage.load();
            GameCategories categories = categoryStorage.load();
            SwingUtilities.invokeLater(() -> new GameLibraryFrame(
                    games, storage, categories, categoryStorage).setVisible(true));
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Bibliothek oder Kategorien: " + e.getMessage());
            e.printStackTrace();
            showMessage("Fehler beim Lesen der Bibliothek oder Kategorien:\n" + e.getMessage(),
                    "Lesefehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showMessage(String message, String title, int messageType) {
        SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(null, message, title, messageType));
    }
}
