import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GameLibraryData {
    public static List<Game> load() {
        List<Game> games = new ArrayList<>();
        games.add(createWitcher());
        games.add(createBaldursGate());
        games.add(createZelda());
        games.add(createTekken());
        games.add(createRollerCoasterTycoon());
        return games;
    }

    private static Game createWitcher() {
        Game game = new Game(
                "The Witcher 3: Wild Hunt",
                "PLAYSTATION_4",
                DistributionType.PHYSICAL,
                2015);
        game.setDeveloper("CD Projekt Red");
        game.getGenres().add("RPG");
        game.getGenres().add("Open World");
        game.setCondition("COMPLETE_IN_BOX");
        game.setStatus("COMPLETED_100");
        game.setCompletionDate(LocalDate.of(2021, 5, 14));
        game.setRating(10);
        game.setNotes("Platin-Trophäe geholt. Grandioses DLC Blood and Wine.");
        return game;
    }

    private static Game createBaldursGate() {
        Game game = new Game(
                "Baldur's Gate 3",
                "PC",
                DistributionType.DIGITAL,
                2023);
        game.setDeveloper("Larian Studios");
        game.getGenres().add("CRPG");
        game.getGenres().add("Turn-Based");
        game.setStorefront("STEAM");
        game.setStatus("PLAYING");
        game.setRating(9);
        game.setNotes("Akt 3 erreicht mit Paladin-Build.");
        return game;
    }

    private static Game createZelda() {
        Game game = new Game(
                "The Legend of Zelda: Ocarina of Time",
                "NINTENDO_RETRO",
                DistributionType.PHYSICAL,
                1998);
        game.setId("a1b2c3d4-e5f6-4a7b-8c9d-0123456789ab");
        game.setDeveloper("Nintendo EAD");
        game.getGenres().add("Action-Adventure");
        game.getGenres().add("Fantasy");
        game.setSeries("The Legend of Zelda");
        game.setCondition("COMPLETE_IN_BOX");
        game.setStatus("COMPLETED_100");
        game.setCompletionDate(LocalDate.of(2020, 8, 15));
        game.setRating(10);
        game.setNotes("Alle 100 Goldenen Skulltulas und Herzteile gesammelt. Absoluter Meilenstein.");
        return game;
    }

    private static Game createTekken() {
        Game game = new Game(
                "Tekken 3",
                "PLAYSTATION_RETRO",
                DistributionType.PHYSICAL,
                1998);
        game.setId("b2c3d4e5-f6a7-4b8c-9d0e-123456789abc");
        game.setDeveloper("Namco");
        game.getGenres().add("Fighting");
        game.getGenres().add("Beat 'em up");
        game.setSeries("Tekken");
        game.setCondition("GOOD");
        game.setStatus("BEATEN");
        game.setCompletionDate(LocalDate.of(2022, 3, 10));
        game.setRating(9);
        game.setNotes("Arcade-Modus mit allen Kämpfern beendet, Gon und Dr. Bosconovitch freigeschaltet.");
        return game;
    }

    private static Game createRollerCoasterTycoon() {
        Game game = new Game(
                "RollerCoaster Tycoon",
                "PC",
                DistributionType.DIGITAL,
                1999);
        game.setId("c3d4e5f6-a7b8-4c9d-0e1f-23456789abcd");
        game.setDeveloper("Chris Sawyer Productions");
        game.getGenres().add("Wirtschaftssimulation");
        game.getGenres().add("Strategie");
        game.setSeries("RollerCoaster Tycoon");
        game.setStorefront("GOG");
        game.setStatus("PLAYING");
        game.setRating(9);
        game.setNotes("Klassiker. Aktuell am Park 'Diamond Heights'.");
        return game;
    }
}
