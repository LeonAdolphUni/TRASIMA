package dhbw.trasima.v2;

import java.util.List;

/**
 * Dienst, der eine Route von einem Start- zu einem Zielpunkt ermittelt.
 */
public interface IRouteService {

    /**
     * @return Wegpunkte als {latitude, longitude}; der letzte Wegpunkt ist das Ziel
     */
    List<double[]> route(double startLat, double startLon, double targetLat, double targetLon);
}
