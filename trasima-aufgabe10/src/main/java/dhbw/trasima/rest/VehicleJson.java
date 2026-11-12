package dhbw.trasima.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dhbw.trasima.v2.V2Data;

/**
 * JSON-Darstellung eines Fahrzeugs laut Aufgabe 10:
 * {"id": "...", "lat": 49.48, "lon": 8.46, "speed": 50.0, "dir": 90.0}
 * Unbekannte Felder (z. B. von anderen Servern) werden ignoriert.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleJson(String id, double lat, double lon, double speed, double dir) {

    public static VehicleJson of(V2Data data) {
        return new VehicleJson(data.id(), data.latitude(), data.longitude(), data.speed(), data.direction());
    }

    public V2Data toV2Data(String id, long timestamp) {
        return new V2Data(id, lat, lon, speed, dir, timestamp);
    }
}
