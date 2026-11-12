package dhbw.trasima.v2;

/**
 * Geometrie auf der Erdkugel (Naeherungen aus der Vorlesung).
 */
public final class Geo {

    /** Erdradius in km. */
    public static final double EARTH_RADIUS_KM = 6370.0;

    private Geo() {
    }

    /**
     * Euler-Schritt: neue Position nach deltaT Sekunden Fahrt.
     * Nur fuer kleine deltaT (kleiner 10 s) und nicht in Polnaehe geeignet.
     *
     * @return {latitude, longitude} in Grad
     */
    public static double[] move(double latitude, double longitude, double speedKmh, double directionDeg,
            double deltaTSeconds) {
        double theta = Math.toRadians(directionDeg);
        double hours = deltaTSeconds / 3600.0;
        double vNorth = speedKmh * Math.cos(theta);
        double vEast = speedKmh * Math.sin(theta);
        double deltaLat = Math.toDegrees(vNorth / EARTH_RADIUS_KM) * hours;
        double deltaLon = Math.toDegrees(vEast / (EARTH_RADIUS_KM * Math.cos(Math.toRadians(latitude)))) * hours;
        return new double[] { latitude + deltaLat, longitude + deltaLon };
    }

    /** Entfernung zweier Punkte in km (Haversine-Formel). */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = phi2 - phi1;
        double dLambda = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    /** Richtung von Punkt 1 nach Punkt 2 in Grad (Norden = 0, Osten = 90). */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dLambda = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dLambda) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(dLambda);
        return (Math.toDegrees(Math.atan2(y, x)) + 360.0) % 360.0;
    }
}
