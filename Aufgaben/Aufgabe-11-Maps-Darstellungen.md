# Aufgabe 11 – Maps Darstellungen

_Quelle: Moodle, Kurs Verteilte Systeme (inf-vs-1)_

Ziel:Implementieren Sie eine Webanwendung (HTML/JavaScript), die die Fahrzeuge aus dem vorhandenen Trasima-REST-Server abruft und auf einer OpenStreetMap-Karte darstellt. Jedes Fahrzeug soll als Marker angezeigt werden und seine Position regelmäßig aktualisiert werden.

Voraussetzungen:

- 
Der REST-Server ist erreichbar und liefert Fahrzeugdaten im JSON-Format.

- 
Die Schnittstelle GET /api/trasima/vehicles liefert eine Liste aller Fahrzeuge mit mindestens:id, lat, lon, speed, dir

Anforderungen: 

- 
Karte anzeigen

- 
Erstellen Sie eine HTML-Seite, die eine OpenStreetMap-Karte darstellt (z. B. mit der Bibliothek Leaflet).

- 
Initiale Kartenposition und Zoom sind frei wählbar (z. B. auf den Bereich der erwarteten Fahrzeuge).

- 
Fahrzeuge vom REST-Server laden

- 
Laden Sie beim Start der Seite die Fahrzeugliste überGET /api/trasima/vehicles.

- 
Parsen Sie die JSON-Antwort und erstellen Sie für jedes Fahrzeug einen Marker.

- 
Marker aktualisieren (Polling)

- 
Aktualisieren Sie die Fahrzeugpositionen regelmäßig (z. B. alle 5 Sekunden) durch erneuten Aufruf vonGET /api/trasima/vehicles.

- 
Wenn ein Fahrzeug bereits als Marker existiert, soll dessen Position verschoben werden (kein neuer Marker).

- 
Wenn ein Fahrzeug neu ist, soll ein neuer Marker erzeugt werden.

- 
Wenn ein Fahrzeug nicht mehr in der Liste vorkommt, soll der Marker entfernt werden (entspricht Delete am Server).

- 
Anzeige von Fahrzeugdetails

- 
Beim Klick auf einen Marker sollen Fahrzeugdaten angezeigt werden (Popup oder Sidebar), z. B.:

- 
ID

- 
Geschwindigkeit (speed)

- 
Richtung (dir)

- 
aktuelle Koordinaten (lat, lon)

- 
Robustheit (Basis)

- 
Falls der REST-Server nicht erreichbar ist oder ein Fehler zurückkommt, soll die Anwendung eine verständliche Meldung anzeigen (z. B. „Server nicht erreichbar“) und später erneut versuchen.

Zuletzt geändert: Mittwoch, 11. November 2026, 15:17
