package dhbw.trasima.v2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-sicherer Key-Value-Store: Fahrzeug-Id -> letzter bekannter Zustand.
 * Wird von allen TRASIMA-Servern (Socket, gRPC, REST) als Speicher verwendet.
 */
public class VehicleStore {

    private final Map<String, V2Data> vehicles = new ConcurrentHashMap<>();

    public void put(V2Data data) {
        vehicles.put(data.id(), data);
    }

    /** Legt das Fahrzeug nur an, wenn die Id noch frei ist. @return true, wenn angelegt */
    public boolean putIfAbsent(V2Data data) {
        return vehicles.putIfAbsent(data.id(), data) == null;
    }

    /** Ersetzt nur ein vorhandenes Fahrzeug. @return true, wenn es vorhanden war */
    public boolean replace(V2Data data) {
        return vehicles.replace(data.id(), data) != null;
    }

    /** @return der Zustand des Fahrzeugs oder null, wenn es unbekannt ist */
    public V2Data get(String id) {
        return vehicles.get(id);
    }

    /** @return true, wenn das Fahrzeug vorhanden war */
    public boolean remove(String id) {
        return vehicles.remove(id) != null;
    }

    public int size() {
        return vehicles.size();
    }

    /** Alle Fahrzeuge, sortiert nach Id. */
    public List<V2Data> getAll() {
        List<V2Data> result = new ArrayList<>(vehicles.values());
        result.sort(Comparator.comparing(V2Data::id));
        return result;
    }

    /** Alle Fahrzeuge im Umkreis von radiusKm, sortiert nach Id. */
    public List<V2Data> getNearby(double latitude, double longitude, double radiusKm) {
        List<V2Data> result = new ArrayList<>();
        for (V2Data v : vehicles.values()) {
            if (Geo.distanceKm(latitude, longitude, v.latitude(), v.longitude()) <= radiusKm) {
                result.add(v);
            }
        }
        result.sort(Comparator.comparing(V2Data::id));
        return result;
    }
}
