# Aufgabe 10 – REST Dienst und REST Client

_Quelle: Moodle, Kurs Verteilte Systeme (inf-vs-1)_

Nun soll die Kommunikation konsequent auf RESTful Services setzen.

Server-Seite: 

- Implementieren Sie einen REST-basierten Trasima Informationsserver für virtuelle Fahrzeuge (V2) mit Java und JAX-RS.

- Der Server stellt seine Funktionalität über HTTP bereit und verwaltet Positions- und Bewegungsdaten von Fahrzeugen. Für jedes Fahrzeug soll die folgende Information gespeichert werden lat (Breitengrad), lon (Längengrad), speed (Geschwindigkeit) und dir (Richtung). 

- Jedes Fahrzeug besitzt eine eindeutige ID, die vom Client vorgegeben wird.

- Die Kommunikation soll im JSON-Format erfolgen.

- 
Der Server soll zustandslos arbeiten. 

Die REST-Schnittstelle soll folgende Dienste bereitstellen: 

- Ausgabe der Daten aller Fahrzeuge als Liste

- Ausgabe der Daten eines Fahrzeugs

- das Erzeugen eines Fahrzeugs mit einer ID

- Falls die ID bereits existiert, soll eine Fehlermeldung zurückgegeben werden.

- die Aktualisierung der Daten eines Fahrzeugs

- die Löschung des Fahrzeugs

- wenn das Fahrzeug sein Ziel erreicht hat 

- Die REST Services sollten die folgenden Werte liefern 

- 
GET /api/trasima/vehicles (Liste)

- 
GET /api/trasima/vehicles/{id} (ein Fahrzeug)

- 
POST /api/trasima/vehicles/{id} (Create mit Client-ID)

- 
PUT /api/trasima/vehicles/{id} (Update)

- 
DELETE /api/trasima/vehicles/{id} (Delete bei Ziel erreicht)

Client-Seite: 

- Implementieren Sie die Schnittstelle IPublishPosition als REST Client

Zuletzt geändert: Mittwoch, 4. November 2026, 14:54
