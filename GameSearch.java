import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public final class GameSearch {
    private GameSearch() {
    }

    public static boolean matches(Game game, String query, GameCategories categories) {
        if (query == null || query.isBlank()) {
            return true;
        }

        StringJoiner searchable = new StringJoiner(" ");
        searchable.add(safe(game.getTitle()));
        searchable.add(safe(game.getDeveloper()));
        searchable.add(safe(game.getSeries()));
        searchable.add(game.getGenres() == null ? "" : String.join(" ", game.getGenres()));
        searchable.add(categories.label(GameCategories.Category.PLATFORM, game.getPlatform()));
        searchable.add(categories.label(
                GameCategories.Category.DISTRIBUTION_TYPE, game.getDistributionType()));
        searchable.add(categories.label(GameCategories.Category.STOREFRONT, game.getStorefront()));
        searchable.add(categories.label(GameCategories.Category.CONDITION, game.getCondition()));
        searchable.add(categories.label(GameCategories.Category.PLAY_STATUS, game.getStatus()));
        searchable.add(safe(game.getNotes()));
        searchable.add(Integer.toString(game.getReleaseYear()));
        if (game.getRating() > 0) {
            searchable.add(Integer.toString(game.getRating()));
        }

        return searchable.toString().toLowerCase(Locale.ROOT)
                .contains(query.trim().toLowerCase(Locale.ROOT));
    }

    public static List<Game> filter(List<Game> games, String query, GameCategories categories) {
        return games.stream()
                .filter(game -> matches(game, query, categories))
                .toList();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
