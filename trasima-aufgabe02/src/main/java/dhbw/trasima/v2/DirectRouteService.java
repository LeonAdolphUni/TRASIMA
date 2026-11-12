package dhbw.trasima.v2;

import java.util.List;

/**
 * Einfachste Route: Luftlinie direkt zum Ziel.
 */
public class DirectRouteService implements IRouteService {

    @Override
    public List<double[]> route(double startLat, double startLon, double targetLat, double targetLon) {
        return List.of(new double[] { targetLat, targetLon });
    }
}
