package dhbw.trasima.v2;

import java.util.List;

/**
 * Schnittstelle, ueber die sich ein V2 ueber andere V2s informieren kann.
 */
public interface ITrafficInfo {

    /** Alle bekannten Fahrzeuge. */
    List<V2Data> getAll();

    /** Alle Fahrzeuge im Umkreis von radiusKm um den angegebenen Punkt. */
    List<V2Data> getNearby(double latitude, double longitude, double radiusKm);
}
