#!/usr/bin/env bash
# Aufgabe 09: Gesamtsystem starten und testen.
#
#   V2s --gRPC--> TRASIMA gRPC-Server <--gRPC-- Servlet (Jetty) <--HTTP-- Browser
#
# Aufruf: ./run-system.sh [anzahl V2s] [-Dname=wert ...]
# Beenden mit Strg+C; die Server werden dann mit beendet.
set -euo pipefail
cd "$(dirname "$0")"

count="${1:-5}"
[ $# -gt 0 ] && shift

pids=()
cleanup() {
  echo
  echo "Beende Server ..."
  for pid in "${pids[@]}"; do kill "$pid" 2>/dev/null || true; done
  wait 2>/dev/null || true
}
trap cleanup EXIT
trap "exit 130" INT TERM

./run.sh grpc-server &
pids+=($!)
sleep 6
./run.sh web &
pids+=($!)
sleep 6

echo
echo "Webseite:  http://localhost:8081/vehicles"
echo

./run.sh grpc-cars "$@" "$count"

echo
echo "Alle V2s sind am Ziel. Die Webseite bleibt erreichbar, bis du Strg+C drueckst."
wait
