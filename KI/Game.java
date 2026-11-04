import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Hauptklasse für ein Spiel
public class Game {
    private String id;
    private String title;
    private String developer;
    private int releaseYear;
    private List<String> genres;
    private String series;

    private String platform;
    private String distributionType;
    private String storefront;

    private String condition;
    private String status;
    private LocalDate completionDate;
    private int rating;
    private String notes;

    // Standard-Konstruktor erzeugt direkt eine eindeutige ID
    public Game() {
        this.id = UUID.randomUUID().toString();
        this.genres = new ArrayList<>();
    }

    // Komfort-Konstruktor
    public Game(String title, String platform, String distributionType, int releaseYear) {
        this();
        this.title = title;
        this.platform = platform;
        this.distributionType = distributionType;
        this.releaseYear = releaseYear;
        this.status = "UNPLAYED";
        this.condition = "DIGITAL".equals(distributionType) ? "NOT_APPLICABLE" : "GOOD";
        this.storefront = "NONE";
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

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getDistributionType() { return distributionType; }
    public void setDistributionType(String distributionType) { this.distributionType = distributionType; }

    public String getStorefront() { return storefront; }
    public void setStorefront(String storefront) { this.storefront = storefront; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
