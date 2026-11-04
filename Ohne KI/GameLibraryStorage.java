import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GameLibraryStorage {
    private static final String FILE_NAME = "spielebibliothek.properties";

    public static List<Game> load() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return GameLibraryData.load();
        }

        Properties data = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            data.load(input);
        }

        List<Game> games = new ArrayList<>();
        int count = Integer.parseInt(data.getProperty("count", "0"));

        for (int i = 0; i < count; i++) {
            String prefix = "game." + i + ".";
            Game game = new Game(
                    data.getProperty(prefix + "title", ""),
                    data.getProperty(prefix + "platform", ""),
                    readEnum(DistributionType.class, data.getProperty(prefix + "type")),
                    Integer.parseInt(data.getProperty(prefix + "year", "0")));

            game.setId(data.getProperty(prefix + "id", game.getId()));
            game.setDeveloper(data.getProperty(prefix + "developer"));
            game.setSeries(data.getProperty(prefix + "series"));
            game.setStorefront(data.getProperty(prefix + "store", ""));
            game.setCondition(data.getProperty(prefix + "condition", ""));
            game.setStatus(data.getProperty(prefix + "status", ""));
            game.setRating(Integer.parseInt(data.getProperty(prefix + "rating", "0")));
            game.setNotes(data.getProperty(prefix + "notes"));

            String date = data.getProperty(prefix + "completionDate", "");
            if (!date.isBlank()) {
                game.setCompletionDate(LocalDate.parse(date));
            }

            String genres = data.getProperty(prefix + "genres", "");
            if (!genres.isBlank()) {
                for (String genre : genres.split(",")) {
                    game.getGenres().add(genre.trim());
                }
            }

            games.add(game);
        }

        return games;
    }

    public static void save(List<Game> games) throws IOException {
        Properties data = new Properties();
        data.setProperty("count", String.valueOf(games.size()));

        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            String prefix = "game." + i + ".";
            data.setProperty(prefix + "id", game.getId());
            data.setProperty(prefix + "title", textOrEmpty(game.getTitle()));
            data.setProperty(prefix + "developer", textOrEmpty(game.getDeveloper()));
            data.setProperty(prefix + "year", String.valueOf(game.getReleaseYear()));
            data.setProperty(prefix + "genres", String.join(", ", game.getGenres()));
            data.setProperty(prefix + "series", textOrEmpty(game.getSeries()));
            data.setProperty(prefix + "platform", textOrEmpty(game.getPlatform()));
            data.setProperty(prefix + "type", nameOrEmpty(game.getDistributionType()));
            data.setProperty(prefix + "store", textOrEmpty(game.getStorefront()));
            data.setProperty(prefix + "condition", textOrEmpty(game.getCondition()));
            data.setProperty(prefix + "status", textOrEmpty(game.getStatus()));
            data.setProperty(prefix + "completionDate", game.getCompletionDate() == null
                    ? "" : game.getCompletionDate().toString());
            data.setProperty(prefix + "rating", String.valueOf(game.getRating()));
            data.setProperty(prefix + "notes", textOrEmpty(game.getNotes()));
        }

        try (FileOutputStream output = new FileOutputStream(FILE_NAME)) {
            data.store(output, "Spielebibliothek");
        }
    }

    private static String textOrEmpty(String text) {
        return text == null ? "" : text;
    }

    private static String nameOrEmpty(Enum<?> value) {
        return value == null ? "" : value.name();
    }

    private static <T extends Enum<T>> T readEnum(Class<T> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Enum.valueOf(type, value);
    }
}
