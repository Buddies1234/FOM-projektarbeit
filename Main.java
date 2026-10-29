import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Erstmal stumpf zwei spiele als Demo anlegen und als json speichern
public class Main {
    public static void main(String[] args) {
        GameJsonStorage storage = new GameJsonStorage("spielebibliothek.json");

        // 1. Liste vorbereiten
        List<Game> myGames = new ArrayList<>();

        // Spiel 1: Physisch auf Konsole
        Game witcher = new Game("The Witcher 3: Wild Hunt", Platform.PLAYSTATION_4, DistributionType.PHYSICAL, 2015);
        witcher.setDeveloper("CD Projekt Red");
        witcher.setCondition(Condition.COMPLETE_IN_BOX);
        witcher.setStatus(PlayStatus.COMPLETED_100);
        witcher.setCompletionDate(LocalDate.of(2021, 5, 14));
        witcher.setRating(10);
        witcher.setGenres(List.of("RPG", "Open World"));
        witcher.setNotes("Platin-Trophäe geholt. Grandioses DLC Blood and Wine.");
        myGames.add(witcher);

        // Spiel 2: Digital auf PC
        Game baldur = new Game("Baldur's Gate 3", Platform.PC, DistributionType.DIGITAL, 2023);
        baldur.setDeveloper("Larian Studios");
        baldur.setStorefront(Storefront.STEAM);
        baldur.setStatus(PlayStatus.PLAYING);
        baldur.setRating(9);
        baldur.setGenres(List.of("CRPG", "Turn-Based"));
        baldur.setNotes("Akt 3 erreicht mit Paladin-Build.");
        myGames.add(baldur);

        // 2. In Datei speichern
        try {
            storage.save(myGames);
            System.out.println("Spiele erfolgreich in 'spielebibliothek.json' gespeichert!\n");

            // 3. Aus Datei wieder laden
            List<Game> loadedGames = storage.load();
            System.out.println("Geladene Spiele (" + loadedGames.size() + "):");
            for (Game g : loadedGames) {
                System.out.println("- " + g.getTitle() + " [" + g.getPlatform() + "] | Status: " + g.getStatus());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
