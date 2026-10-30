# Spielebibliothek

Die Java-Swing-Anwendung verwaltet Spiele aus der Datei `spielebibliothek.json`.
Zum Starten müssen die Java-Quelldateien kompiliert sein; anschließend wird
`Main` im Projektordner ausgeführt. Fehlt die Datei, startet die Anwendung mit
einer leeren Bibliothek und legt die Datei beim ersten Speichern an.

Über **Neu**, **Bearbeiten** und **Löschen** in der linken Seitenleiste lassen sich
Spiele verwalten. Änderungen werden in `spielebibliothek.json` gespeichert.
Beim Löschen ist eine Bestätigung erforderlich. Die Bibliothek kann auch leer
sein; neue Spiele lassen sich danach weiterhin anlegen.

Die Liste lässt sich über die Sortierauswahl nach Titel, Erscheinungsjahr oder
Plattform sortieren. Mit der Schaltfläche darunter wechselst du zwischen
aufsteigender und absteigender Reihenfolge. Die Sortierung gilt auch nach dem
Anlegen, Bearbeiten oder Löschen weiter.

Über das Suchfeld kannst du die Bibliothek live nach Text filtern, zum Beispiel
nach „Skyrim“. Durchsucht werden unter anderem Titel, Entwickler, Reihe,
Plattform, Genres und Notizen; die Suche unterscheidet nicht zwischen Groß- und
Kleinschreibung.

Das Formular enthält alle Angaben eines Spiels. Genres werden durch Kommas
getrennt eingegeben, ein Abschlussdatum verwendet das Format `JJJJ-MM-TT`,
und `0` bei Erscheinungsjahr oder Bewertung steht für „unbekannt/keine“.

Der Schalter **Admin-Modus: AUS** aktiviert ohne Passwort den Zugang zur
Kategorienverwaltung. Dort lassen sich Plattform, Format, Store, Zustand und
Spielstatus hinzufügen und umbenennen. Umbenannte Werte werden bei allen
Spielen sofort unter dem neuen Namen angezeigt. Werte, die noch von Spielen
verwendet werden, können erst gelöscht werden, nachdem sie in diesen Spielen
geändert wurden. Die Kategorien werden in `spielekategorien.properties`
gespeichert; fehlt die Datei, werden die Standardwerte verwendet.