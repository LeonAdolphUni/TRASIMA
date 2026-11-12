package dhbw.trasima.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeoTest {

    @Test
    void oneDegreeOfLatitudeIsAbout111Km() {
        assertEquals(111.2, Geo.distanceKm(49.0, 8.5, 50.0, 8.5), 0.2);
    }

    @Test
    void bearingFollowsCompass() {
        assertEquals(0.0, Geo.bearing(49.0, 8.5, 49.1, 8.5), 0.01);
        assertEquals(180.0, Geo.bearing(49.1, 8.5, 49.0, 8.5), 0.01);
        assertEquals(90.0, Geo.bearing(49.0, 8.5, 49.0, 8.6), 0.1);
        assertEquals(270.0, Geo.bearing(49.0, 8.6, 49.0, 8.5), 0.1);
    }

    @Test
    void drivingOneMinuteAt60KmhCoversOneKm() {
        for (double direction : new double[] { 0, 45, 90, 200, 315 }) {
            double lat = Mannheim.CENTER_LAT;
            double lon = Mannheim.CENTER_LON;
            for (int i = 0; i < 12; i++) {
                double[] p = Geo.move(lat, lon, 60, direction, 5);
                lat = p[0];
                lon = p[1];
            }
            assertEquals(1.0, Geo.distanceKm(Mannheim.CENTER_LAT, Mannheim.CENTER_LON, lat, lon), 0.005,
                    "Richtung " + direction);
        }
    }
}
