#!/usr/bin/env bash
# Startet ein TRASIMA-Programm.   Aufruf: ./run.sh <programm> [-Dname=wert ...] [argumente ...]
# Ohne Argumente wird die Liste der Programme angezeigt.
set -euo pipefail
cd "$(dirname "$0")"
source ./env.sh

usage() {
  cat <<'TXT'
Aufruf: ./run.sh <programm> [-Dname=wert ...] [argumente ...]

  build                 alles bauen und testen (mvn install)

  hello                 Aufgabe 01: Hello TRASIMA
  sim [anzahl]          Aufgabe 02: Simulation ohne Netzwerk
  socket-server-single [port]                     Aufgabe 04: Single-Threaded Server (Standard-Port 7001)
  socket-server [port] [virtual|platform]         Aufgabe 05: Multi-Threaded Server  (Standard-Port 7001)
  socket-cars [anzahl] [praefix] [host] [port]    V2s melden per Socket
  grpc-demo-server [port]                         Aufgabe 06: gRPC-Demo aus der Vorlesung (Standard-Port 8080)
  grpc-demo-client [port]
  grpc-server [port]                              Aufgabe 07: gRPC-Server (Standard-Port 50051)
  grpc-cars [anzahl] [praefix] [host] [port]      V2s melden per gRPC
  web [http-port] [grpc-host] [grpc-port]         Aufgabe 08: Webseite (Standard-Port 8081)
  rest-server [port]                              Aufgabe 10: REST-Server (Standard-Port 8082)
  rest-cars [anzahl] [praefix] [basis-url]        V2s melden per REST
  maps [port]                                     Aufgabe 11: REST-Server mit OpenStreetMap-Karte (Standard-Port 8083)

Einstellungen der V2s:
  -Dtrasima.sleep=1000   reale Wartezeit pro Schritt in ms
  -Dtrasima.dt=5         simulierte Sekunden pro Schritt
  -Dtrasima.route=osrm   Route ueber Internet-Dienst (osrm) oder Luftlinie (direct)
TXT
}

[ $# -ge 1 ] || { usage; exit 1; }
program="$1"; shift

case "$program" in
  build)                exec mvn -B install ;;
  hello)                module=trasima-aufgabe01; main=dhbw.trasima.App ;;
  sim)                  module=trasima-aufgabe02; main=dhbw.trasima.v2.SimulationApp ;;
  socket-server-single) module=trasima-aufgabe04; main=dhbw.trasima.socket.SingleThreadedServer ;;
  socket-cars)          module=trasima-aufgabe04; main=dhbw.trasima.socket.SocketCarsApp ;;
  socket-server)        module=trasima-aufgabe05; main=dhbw.trasima.socket.MultiThreadedServer ;;
  grpc-demo-server)     module=trasima-aufgabe06; main=dhbw.trasima.grpc.demo.ServerApp ;;
  grpc-demo-client)     module=trasima-aufgabe06; main=dhbw.trasima.grpc.demo.ClientApp ;;
  grpc-server)          module=trasima-aufgabe07; main=dhbw.trasima.grpc.GrpcServerApp ;;
  grpc-cars)            module=trasima-aufgabe07; main=dhbw.trasima.grpc.GrpcCarsApp ;;
  web)                  module=trasima-aufgabe08; main=dhbw.trasima.web.WebServerApp ;;
  rest-server)          module=trasima-aufgabe10; main=dhbw.trasima.rest.RestServer ;;
  rest-cars)            module=trasima-aufgabe10; main=dhbw.trasima.rest.RestCarsApp ;;
  maps)                 module=trasima-aufgabe11; main=dhbw.trasima.maps.MapsServer ;;
  -h|--help|help)       usage; exit 0 ;;
  *)                    echo "Unbekanntes Programm: $program" >&2; usage; exit 1 ;;
esac

# Beim ersten Mal alles bauen, damit die Module im lokalen Maven-Repository liegen
if [ ! -f trasima-aufgabe11/target/trasima-aufgabe11-1.0.0-SNAPSHOT.jar ]; then
  echo "Erster Start: baue das Projekt ..." >&2
  mvn -q -B -DskipTests install
fi

props=()
while [ $# -gt 0 ] && [[ "$1" == -D* ]]; do props+=("$1"); shift; done

exec mvn -q -B -pl "$module" "${props[@]}" exec:java -Dexec.mainClass="$main" -Dexec.args="$*"
