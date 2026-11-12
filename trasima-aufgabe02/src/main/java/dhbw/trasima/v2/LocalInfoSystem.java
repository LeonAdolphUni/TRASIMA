package dhbw.trasima.v2;

import java.util.List;

/**
 * Informationssystem im selben Prozess: speichert die Positionen direkt in einem VehicleStore.
 * Damit laeuft die Simulation ohne Netzwerk (Aufgabe 02).
 */
public class LocalInfoSystem implements IPositionPublisher, ITrafficInfo {

    private final VehicleStore store;

    public LocalInfoSystem() {
        this(new VehicleStore());
    }

    public LocalInfoSystem(VehicleStore store) {
        this.store = store;
    }

    @Override
    public void publish(V2Data data) {
        store.put(data);
    }

    @Override
    public List<V2Data> getAll() {
        return store.getAll();
    }

    @Override
    public List<V2Data> getNearby(double latitude, double longitude, double radiusKm) {
        return store.getNearby(latitude, longitude, radiusKm);
    }
}
