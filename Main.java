import java.io.File;
import java.io.IOException;
import java.util.List;

//erzeugt jetzt nichts mehr, sondern liest die bestehende spielebibliothek ein
public class Main {
    public static void main(String[] args) {
        String filePath = "spielebibliothek.json";
        File file = new File(filePath);

        // Prüfen, ob die Datei überhaupt existiert
        if (!file.exists()) {
            System.out.println("Die Datei '" + filePath + "' wurde nicht gefunden!");
            System.out.println("Stelle sicher, dass sie im Projektordner liegt.");
            return;
        }

        GameJsonStorage storage = new GameJsonStorage(filePath);

        try {
            // Spiele aus der JSON-Datei laden
            List<Game> games = storage.load();

            if (games.isEmpty()) {
                System.out.println("Die Bibliothek ist leer (keine Spiele gefunden).");
                return;
            }

            System.out.println("=== Spielebibliothek geladen (" + games.size() + " Einträge) ===");
            System.out.println();

            for (int i = 0; i < games.size(); i++) {
                Game g = games.get(i);
                System.out.println("[" + (i + 1) + "] " + g.getTitle() + " (" + g.getReleaseYear() + ")");
                System.out.println("    ID:         " + g.getId());
                System.out.println("    Entwickler: " + (g.getDeveloper() != null ? g.getDeveloper() : "-"));
                System.out.println("    Plattform:  " + g.getPlatform() + " (" + g.getDistributionType() + ")");
                
                if (g.getDistributionType() == DistributionType.DIGITAL) {
                    System.out.println("    Store:      " + g.getStorefront());
                } else {
                    System.out.println("    Zustand:    " + g.getCondition());
                }

                System.out.println("    Status:     " + g.getStatus());
                if (g.getCompletionDate() != null) {
                    System.out.println("    Beendet am: " + g.getCompletionDate());
                }
                System.out.println("    Bewertung:  " + (g.getRating() > 0 ? g.getRating() + "/10" : "Keine"));
                if (g.getGenres() != null && !g.getGenres().isEmpty()) {
                    System.out.println("    Genres:     " + String.join(", ", g.getGenres()));
                }
                if (g.getNotes() != null && !g.getNotes().isBlank()) {
                    System.out.println("    Notizen:    " + g.getNotes());
                }
                System.out.println("--------------------------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
