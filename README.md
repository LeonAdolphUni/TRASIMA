# TRASIMA – Traffic Simulation Mannheim

Projekt zur Vorlesung „Verteilte Systeme“ (Prof. Dr. Kornmayer, DHBW Mannheim).
Virtual Vehicles (V2) fahren autonom durch Mannheim, melden Position und Geschwindigkeit an einen
zentralen Key-Value-Store und informieren sich dort über andere V2s in ihrer Nähe.

## Voraussetzungen

Java 21 und Maven 3.9. Liegen sie wie auf dem Entwicklungsrechner unter `~/.local/opt`, in jeder neuen Shell einmal aktivieren (sonst werden `java`/`mvn` aus dem PATH genutzt):

```bash
source env.sh
```

Die Skripte `run.sh` und `run-system.sh` machen das selbst.

## Schnellstart

```bash
./run.sh build          # alles bauen und alle Tests ausführen
./run.sh sim 3          # Aufgabe 02: drei V2s ohne Netzwerk
./run-system.sh 5       # Aufgabe 09: Gesamtsystem mit fünf V2s
./run-maps.sh 5         # Aufgabe 11: V2s per REST auf einer OpenStreetMap-Karte
```

Bei `run-maps.sh` zeigt http://localhost:8083/ die Fahrzeuge live auf einer OpenStreetMap-Karte.
Beim Gesamtsystem zeigt http://localhost:8081/vehicles alle Fahrzeuge als Tabelle und Lageplan.
`./run.sh` ohne Argumente listet alle Programme auf.

## Module und Aufgaben

| Modul | Aufgabe | Inhalt |
|---|---|---|
| `trasima-aufgabe01` | 01 | Erstes Maven-Modul im hierarchischen Projekt |
| `trasima-aufgabe02` | 02 | Modell des V2 (Euler-Näherung), Route, Dependency Injection über `IPositionPublisher` und `ITrafficInfo`, `VehicleStore` |
| `trasima-aufgabe04` | 04 | Socket-Protokoll, `CommandProcessor`, Single-Threaded Server, Socket-Client |
| `trasima-aufgabe05` | 05 | Multi-Threaded Server mit Virtual Threads (wahlweise Thread-Pool) |
| `trasima-aufgabe06` | 06 | gRPC-Demo aus der Vorlesung (`MyDemoService`) |
| `trasima-aufgabe07` | 07 | TRASIMA-Server und Publisher über gRPC |
| `trasima-aufgabe08` | 08 | Servlet im eingebetteten Jetty: Webseite mit allen Fahrzeugen des gRPC-Servers |
| `run-system.sh` | 09 | Gesamtsystem starten und testen |
| `trasima-aufgabe10` | 10 | Informationssystem als REST-Server (JAX-RS/Jersey, `/api/trasima/vehicles`) und REST-Client |
| `trasima-aufgabe11` | 11 | Webanwendung (HTML/JavaScript, Leaflet): Fahrzeuge vom REST-Server auf einer OpenStreetMap-Karte |

Die Aufgabentexte aus Moodle liegen in [`Aufgaben/`](Aufgaben/). Aufgabe 03 (Dependency Injection mit
`IPositionPublisher` und austauschbaren Implementierungen) steckt bereits in Modul 02; ein eigenes
Modul dafür gibt es nicht.

## Architektur

```
                    IPositionPublisher / ITrafficInfo
V2 (Thread) ──────────────┬───────────────┬───────────────┐
                          │               │               │
                   Socket-Client     gRPC-Client     REST-Client
                          │               │               │
                    Socket-Server    gRPC-Server     REST-Server
                    (Port 7001)      (Port 50051)    (Port 8082, mit Karte 8083)
                          └───────────────┴───────────────┘
                                    VehicleStore
                                          ▲
                              gRPC        │
              Servlet (Port 8081) ────────┘   ◄── HTTP ── Browser
```

Das V2 kennt nur die beiden Schnittstellen. Welcher Client dahintersteckt, wird beim Start
zusammengebaut (`SocketCarsApp`, `GrpcCarsApp`, `RestCarsApp`).

Jeder Server hat seinen eigenen `VehicleStore`. Ein V2, das per Socket meldet, erscheint also
nicht im gRPC- oder REST-Server.

## Die Varianten einzeln starten

Jeweils zwei Terminals: zuerst der Server, dann die V2s.

```bash
# Sockets (Aufgabe 04/05)
./run.sh socket-server            # oder: socket-server-single
./run.sh socket-cars 3

# gRPC (Aufgabe 07) mit Webseite (Aufgabe 08)
./run.sh grpc-server
./run.sh web
./run.sh grpc-cars 3

# REST (Aufgabe 10)
./run.sh rest-server
./run.sh rest-cars 3

# Karte (Aufgabe 11): REST-Server mit Karte auf Port 8083
./run.sh maps
./run.sh rest-cars 3 V2- http://localhost:8083/api
```

## Aufgabe 11: Kartenanwendung

`MapsServer` startet den REST-Dienst aus Aufgabe 10 und liefert zusätzlich die Karte:

| Pfad | Inhalt |
|---|---|
| `/` | Kartenanwendung (`src/main/resources/maps/index.html`, Leaflet + OpenStreetMap) |
| `/api/trasima/vehicles` | REST-Dienst aus Aufgabe 10: die V2s melden sich hier, die Karte liest die Liste (`id`, `lat`, `lon`, `speed`, `dir`) |

Die Seite fragt alle 5 Sekunden die Fahrzeugliste ab. Bekannte Fahrzeuge werden verschoben,
neue bekommen einen Marker, verschwundene (V2 am Ziel, `DELETE /api/trasima/vehicles/{id}`) werden entfernt.
Ein Klick auf einen Marker oder einen Eintrag in der Liste zeigt ID, Geschwindigkeit, Richtung und
Koordinaten. Ist der Server nicht erreichbar, erscheint „Server nicht erreichbar“ und die Seite
versucht es weiter. Andere Quelle oder Intervall: `http://localhost:8083/?api=<url>&interval=2`.
Der Server erlaubt CORS, die `index.html` lässt sich also auch direkt als Datei öffnen.

Der Socket-Server lässt sich von Hand testen, z. B. mit `nc localhost 7001`:

```
PUT V2-1 49.4879 8.4662 50 90
GET V2-1
NEAR 49.4879 8.4662 0.5
LIST
QUIT
```

Der REST-Server (Schnittstelle laut Aufgabe 10) mit curl:

```bash
curl -i http://localhost:8082/api/trasima/vehicles                      # Liste
curl -i -X POST http://localhost:8082/api/trasima/vehicles/4711 \
     -H "Content-Type: application/json" -d '{"lat":49.48,"lon":8.46,"speed":30,"dir":10}'   # Create (409, wenn vorhanden)
curl -i -X PUT  http://localhost:8082/api/trasima/vehicles/4711 \
     -H "Content-Type: application/json" -d '{"lat":49.50,"lon":8.50,"speed":50,"dir":90}'   # Update (404, wenn unbekannt)
curl -i http://localhost:8082/api/trasima/vehicles/4711                 # ein Fahrzeug
curl -i -X DELETE http://localhost:8082/api/trasima/vehicles/4711       # Delete (Ziel erreicht)
```

Der REST-Client meldet ein V2 zuerst mit POST an, danach mit PUT und löscht es am Ziel mit DELETE.

### Fahrzeuge auf dem Kursserver testen

Die Karte des Dozenten liegt unter http://[2001:7c0:1b20:c913:1::51d]:8080/trasima (nur per VPN).
Eigene V2s dorthin schicken, mit einem eigenen Id-Präfix, damit es keine Kollision mit anderen gibt:

```bash
./run.sh rest-cars 3 Leon- 'http://[2001:7c0:1b20:c913:1::51d]:8080/trasima/api'
```

Die Basis-URL endet vor `/trasima/vehicles`. Liegt die Schnittstelle auf dem Kursserver
woanders, nur diese URL anpassen.

## Einstellungen der V2s

Als `-D`-Optionen direkt nach dem Programmnamen, z. B. `./run.sh sim -Dtrasima.sleep=100 5`:

| Option | Standard | Bedeutung |
|---|---|---|
| `-Dtrasima.sleep` | `1000` | reale Wartezeit pro Schritt in ms |
| `-Dtrasima.dt` | `5` | simulierte Sekunden pro Schritt (Euler-Näherung, unter 10 bleiben) |
| `-Dtrasima.route` | `osrm` | `osrm`: Route über den Internet-Dienst router.project-osrm.org; `direct`: Luftlinie |

Ist der Routen-Dienst nicht erreichbar, fährt das V2 automatisch Luftlinie.

## Ports

| Dienst | Port |
|---|---|
| Socket-Server | 7001 |
| gRPC-Server | 50051 |
| Webseite | 8081 |
| REST-Server | 8082 |
| Karte + REST (Aufgabe 11) | 8083 |
| gRPC-Demo (Aufgabe 06) | 8080 |

Alle Ports lassen sich als Argument ändern. Auf diesem Rechner ist 8080 schon belegt; die Demo
deshalb mit einem anderen Port starten: `./run.sh grpc-demo-server 50061` und
`./run.sh grpc-demo-client 50061`.
