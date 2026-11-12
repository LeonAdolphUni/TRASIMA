package dhbw.trasima.v2;

import java.util.Random;

/**
 * Das Simulationsgebiet: Mannheim.
 */
public final class Mannheim {

    public static final double MIN_LAT = 49.40;
    public static final double MAX_LAT = 49.56;
    public static final double MIN_LON = 8.35;
    public static final double MAX_LON = 8.55;

    public static final double CENTER_LAT = 49.4879;
    public static final double CENTER_LON = 8.4662;

    /** Einige bekannte Orte als {Name, latitude, longitude}. */
    public static final Object[][] PLACES = {
            { "DHBW Coblitzallee", 49.4741, 8.5343 },
            { "Hauptbahnhof", 49.4796, 8.4699 },
            { "Wasserturm", 49.4840, 8.4756 },
            { "Schloss", 49.4832, 8.4622 },
            { "Luisenpark", 49.4795, 8.4960 },
            { "SAP Arena", 49.4636, 8.5176 },
            { "Alte Feuerwache", 49.4950, 8.4720 },
            { "Uniklinikum", 49.4930, 8.4870 },
            { "Käfertal", 49.5090, 8.5170 },
            { "Neckarau", 49.4550, 8.4880 },
    };

    private Mannheim() {
    }

    public static double[] place(int index) {
        Object[] p = PLACES[Math.floorMod(index, PLACES.length)];
        return new double[] { (Double) p[1], (Double) p[2] };
    }

    public static String placeName(int index) {
        return (String) PLACES[Math.floorMod(index, PLACES.length)][0];
    }

    public static boolean contains(double latitude, double longitude) {
        return latitude > MIN_LAT && latitude < MAX_LAT && longitude > MIN_LON && longitude < MAX_LON;
    }

    /** Zufaelliger Punkt im inneren Stadtgebiet. */
    public static double[] randomPoint(Random random) {
        double lat = 49.45 + random.nextDouble() * 0.07;
        double lon = 8.42 + random.nextDouble() * 0.11;
        return new double[] { lat, lon };
    }
}
