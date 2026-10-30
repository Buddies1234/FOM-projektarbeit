import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

public class GameJsonStorage {

    private final File storageFile;

    public GameJsonStorage(String filePath) {
        this.storageFile = new File(filePath);
    }

    public void save(List<Game> games) throws IOException {
        File parentDir = storageFile.getParentFile();
        if (parentDir != null) {
            Files.createDirectories(parentDir.toPath());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < games.size(); i++) {
            Game g = games.get(i);
            sb.append("  {\n");
            appendField(sb, "id", g.getId(), true);
            appendField(sb, "title", g.getTitle(), true);
            appendField(sb, "developer", g.getDeveloper(), true);
            appendNumber(sb, "releaseYear", g.getReleaseYear());
            appendStringList(sb, "genres", g.getGenres());
            appendField(sb, "series", g.getSeries(), true);
            appendField(sb, "platform", g.getPlatform(), true);
            appendField(sb, "distributionType", g.getDistributionType(), true);
            appendField(sb, "storefront", g.getStorefront(), true);
            appendField(sb, "condition", g.getCondition(), true);
            appendField(sb, "status", g.getStatus(), true);
            appendField(sb, "completionDate", g.getCompletionDate() != null ? g.getCompletionDate().toString() : null, true);
            appendNumber(sb, "rating", g.getRating());
            appendField(sb, "notes", g.getNotes(), false);
            sb.append("  }");

            if (i < games.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("]");

        File tempDirectory = parentDir == null ? new File(".") : parentDir;
        File tempFile = File.createTempFile("spielebibliothek-", ".tmp", tempDirectory);
        try {
            Files.writeString(tempFile.toPath(), sb.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(tempFile.toPath(), storageFile.toPath(),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }

    public List<Game> load() throws IOException {
        List<Game> games = new ArrayList<>();
        if (!storageFile.exists() || storageFile.length() == 0) {
            return games;
        }

        String content = Files.readString(storageFile.toPath(), StandardCharsets.UTF_8).trim();
        if (!content.startsWith("[") || !content.endsWith("]")) {
            return games;
        }

        content = content.substring(1, content.length() - 1).trim();
        if (content.isEmpty()) {
            return games;
        }

        String[] rawObjects = content.split("(?<=\\})\\s*,\\s*(?=\\{)");

        for (String raw : rawObjects) {
            Map<String, String> map = parseJsonObject(raw);
            Game g = new Game();

            // Falls die Datei schon eine ID hat, diese übernehmen, sonst bleibt die neu generierte erhalten
            if (map.get("id") != null && !map.get("id").isBlank()) {
                g.setId(map.get("id"));
            }

            g.setTitle(map.get("title"));
            g.setDeveloper(map.get("developer"));
            g.setSeries(map.get("series"));
            g.setNotes(map.get("notes"));

            if (map.containsKey("releaseYear")) {
                g.setReleaseYear(Integer.parseInt(map.get("releaseYear")));
            }
            if (map.containsKey("rating")) {
                g.setRating(Integer.parseInt(map.get("rating")));
            }
            if (map.get("platform") != null) {
                g.setPlatform(map.get("platform"));
            }
            if (map.get("distributionType") != null) {
                g.setDistributionType(map.get("distributionType"));
            }
            if (map.get("storefront") != null) {
                g.setStorefront(map.get("storefront"));
            }
            if (map.get("condition") != null) {
                g.setCondition(map.get("condition"));
            }
            if (map.get("status") != null) {
                g.setStatus(map.get("status"));
            }
            if (map.get("completionDate") != null) {
                g.setCompletionDate(LocalDate.parse(map.get("completionDate")));
            }
            g.setGenres(parseStringArray(raw, "genres"));

            games.add(g);
        }

        return games;
    }

    private List<String> parseStringArray(String object, String key) throws IOException {
        List<String> values = new ArrayList<>();
        String field = "\"" + key + "\"";
        int fieldIndex = object.indexOf(field);
        if (fieldIndex == -1) {
            return values;
        }

        int arrayStart = object.indexOf('[', fieldIndex + field.length());
        if (arrayStart == -1) {
            return values;
        }

        int index = arrayStart + 1;
        while (index < object.length()) {
            char current = object.charAt(index);
            if (Character.isWhitespace(current) || current == ',') {
                index++;
                continue;
            }
            if (current == ']') {
                return values;
            }
            if (current != '"') {
                throw new IOException("Ungültiges JSON-Array für '" + key + "'.");
            }

            StringBuilder value = new StringBuilder();
            index++;
            boolean escaped = false;
            boolean closed = false;
            while (index < object.length()) {
                current = object.charAt(index++);
                if (escaped) {
                    value.append('\\').append(current);
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    closed = true;
                    break;
                } else {
                    value.append(current);
                }
            }

            if (!closed || escaped) {
                throw new IOException("Unvollständiger JSON-Text in '" + key + "'.");
            }
            values.add(unescapeJson(value.toString()));
        }

        throw new IOException("Nicht geschlossenes JSON-Array für '" + key + "'.");
    }

    private String unescapeJson(String value) throws IOException {
        StringBuilder unescaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current != '\\') {
                unescaped.append(current);
                continue;
            }
            if (++i >= value.length()) {
                throw new IOException("Unvollständige Escape-Sequenz im JSON-Text.");
            }

            char escaped = value.charAt(i);
            switch (escaped) {
                case '"': unescaped.append('"'); break;
                case '\\': unescaped.append('\\'); break;
                case '/': unescaped.append('/'); break;
                case 'b': unescaped.append('\b'); break;
                case 'f': unescaped.append('\f'); break;
                case 'n': unescaped.append('\n'); break;
                case 'r': unescaped.append('\r'); break;
                case 't': unescaped.append('\t'); break;
                case 'u':
                    if (i + 4 >= value.length()) {
                        throw new IOException("Unvollständige Unicode-Escape-Sequenz im JSON-Text.");
                    }
                    try {
                        unescaped.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                    } catch (NumberFormatException e) {
                        throw new IOException("Ungültige Unicode-Escape-Sequenz im JSON-Text.", e);
                    }
                    i += 4;
                    break;
                default:
                    throw new IOException("Ungültige Escape-Sequenz im JSON-Text.");
            }
        }
        return unescaped.toString();
    }

    private void appendField(StringBuilder sb, String key, String value, boolean trailingComma) {
        sb.append("    \"").append(key).append("\": ");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append("\"").append(escapeJson(value)).append("\"");
        }
        if (trailingComma) sb.append(",");
        sb.append("\n");
    }

    private void appendNumber(StringBuilder sb, String key, int value) {
        sb.append("    \"").append(key).append("\": ").append(value).append(",\n");
    }

    private void appendStringList(StringBuilder sb, String key, List<String> list) {
        sb.append("    \"").append(key).append("\": [");
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                sb.append("\"").append(escapeJson(list.get(i))).append("\"");
                if (i < list.size() - 1) sb.append(", ");
            }
        }
        sb.append("],\n");
    }

    private String escapeJson(String s) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char current = s.charAt(i);
            switch (current) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (current < 0x20) {
                        escaped.append("\\u");
                        String hex = Integer.toHexString(current);
                        for (int padding = hex.length(); padding < 4; padding++) {
                            escaped.append('0');
                        }
                        escaped.append(hex);
                    } else {
                        escaped.append(current);
                    }
            }
        }
        return escaped.toString();
    }

    private Map<String, String> parseJsonObject(String block) throws IOException {
        Map<String, String> map = new HashMap<>();
        String[] lines = block.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("{") || line.startsWith("}") || line.isBlank()) continue;
            if (line.endsWith(",")) line = line.substring(0, line.length() - 1);

            int colonIdx = line.indexOf(":");
            if (colonIdx == -1) continue;

            String key = line.substring(0, colonIdx).replace("\"", "").trim();
            String val = line.substring(colonIdx + 1).trim();

            if (val.equals("null")) {
                map.put(key, null);
            } else if (val.startsWith("[") && val.endsWith("]")) {
                String inner = val.substring(1, val.length() - 1).replace("\"", "").trim();
                map.put(key, inner);
            } else if (val.startsWith("\"") && val.endsWith("\"")) {
                String str = unescapeJson(val.substring(1, val.length() - 1));
                map.put(key, str);
            } else {
                map.put(key, val);
            }
        }
        return map;
    }
}
