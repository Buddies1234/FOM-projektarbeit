# Spielebibliothek

## Starten

Kompiliere die Java-Dateien und starte anschließend `Main`:

```text
javac Main.java Game.java GameLibraryData.java GameLibraryStorage.java GameLibraryFrame.java
java Main
```

Beim ersten Start werden die Spiele aus `GameLibraryData.java` geladen.
Neue oder bearbeitete Spiele werden automatisch in
`spielebibliothek.properties` gespeichert und beim nächsten Start wieder geladen.
Plattform, Store, Zustand und Spielstatus können im Formular frei als Text eingegeben werden.