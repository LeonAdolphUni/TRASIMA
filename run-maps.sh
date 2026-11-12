#!/usr/bin/env bash
# Aufgabe 11: REST-Server mit OpenStreetMap-Karte starten und V2s darauf fahren lassen.
#
#   V2s --REST (POST/PUT/DELETE /api/trasima/vehicles/{id})--> MapsServer <--GET /api/trasima/vehicles-- Browser (Leaflet)
#
# Aufruf: ./run-maps.sh [anzahl V2s] [-Dname=wert ...]
# Beenden mit Strg+C; der Server wird dann mit beendet.
set -euo pipefail
cd "$(dirname "$0")"

count="${1:-5}"
[ $# -gt 0 ] && shift
port=8083

pids=()
cleanup() {
  echo
  echo "Beende Server ..."
  for pid in "${pids[@]}"; do kill "$pid" 2>/dev/null || true; done
  wait 2>/dev/null || true
}
trap cleanup EXIT
trap "exit 130" INT TERM

./run.sh maps "$port" &
pids+=($!)
sleep 6

echo
echo "Karte:  http://localhost:$port/"
echo

./run.sh rest-cars "$@" "$count" Leons-Auto- "http://localhost:$port/api"

echo
echo "Alle V2s sind am Ziel. Die Karte bleibt erreichbar, bis du Strg+C drueckst."
wait
