import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//Wenn BIB leer wird ein Demospiel erstellt
public class GameLibraryData {
    public static List<Game> load() {
        List<Game> games = new ArrayList<>();
        games.add(createDemo());
        return games;
    }

    private static Game createDemo() {
        Game game = new Game(
                "DemoGame",
                "DemoKonsole",
                DistributionType.PHYSICAL,
                2015);
        game.setDeveloper("DemoDeveloper");
        game.getGenres().add("RPG");
        game.getGenres().add("Open World");
        game.setCondition("COMPLETE_IN_BOX");
        game.setStatus("COMPLETED_100");
        game.setCompletionDate(LocalDate.of(2020, 1, 1));
        game.setRating(10);
        game.setNotes("Alles geholt. 100%");
        return game;
    }

}
