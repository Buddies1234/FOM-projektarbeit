import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Enums für Auswahllisten
enum Platform {
    PC,
    PLAYSTATION_5,
    PLAYSTATION_4,
    PLAYSTATION_3,
    PLAYSTATION_RETRO,
    NINTENDO_SWITCH,
    NINTENDO_RETRO,
    XBOX_SERIES,
    XBOX_ONE,
    XBOX_RETRO,
    OTHER
}

enum DistributionType {
    PHYSICAL,
    DIGITAL
}

enum Storefront {
    NONE,
    STEAM,
    GOG,
    EPIC_GAMES,
    PLAYSTATION_NETWORK,
    NINTENDO_ESHOP,
    XBOX_STORE,
    OTHER
}

enum Condition {
    NEW_SEALED,         // Neu / OVP
    COMPLETE_IN_BOX,    // Sehr gut (mit Hülle & Anleitung)
    GOOD,               // Normale Gebrauchsspuren
    LOOSE,              // Nur Modul/Disc
    NOT_APPLICABLE      // Für Digital
}

enum PlayStatus {
    UNPLAYED,           // Noch nicht gespielt
    PLAYING,            // Aktuell dran
    BEATEN,             // Durchgespielt (Story)
    COMPLETED_100,      // 100% / Alle Erfolge
    DROPPED             // Abgebrochen
}

// Hauptklasse für ein Spiel
public class Game {
    private String id;
    private String title;
    private String developer;
    private int releaseYear;
    private List<String> genres;
    private String series;

    private Platform platform;
    private DistributionType distributionType;
    private Storefront storefront;

    private Condition condition;
    private PlayStatus status;
    private LocalDate completionDate;
    private int rating;
    private String notes;
    private String coverImagePath;

    // Standard-Konstruktor erzeugt direkt eine eindeutige ID
    public Game() {
        this.id = UUID.randomUUID().toString();
        this.genres = new ArrayList<>();
    }

    // Komfort-Konstruktor
    public Game(String title, Platform platform, DistributionType distributionType, int releaseYear) {
        this();
        this.title = title;
        this.platform = platform;
        this.distributionType = distributionType;
        this.releaseYear = releaseYear;
        this.status = PlayStatus.UNPLAYED;
        this.condition = (distributionType == DistributionType.DIGITAL) 
                         ? Condition.NOT_APPLICABLE 
                         : Condition.GOOD;
        this.storefront = Storefront.NONE;
    }

    // Getter und Setter für die ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Übrige Getter und Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDeveloper() { return developer; }
    public void setDeveloper(String developer) { this.developer = developer; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }

    public DistributionType getDistributionType() { return distributionType; }
    public void setDistributionType(DistributionType distributionType) { this.distributionType = distributionType; }

    public Storefront getStorefront() { return storefront; }
    public void setStorefront(Storefront storefront) { this.storefront = storefront; }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    public PlayStatus getStatus() { return status; }
    public void setStatus(PlayStatus status) { this.status = status; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }
}
