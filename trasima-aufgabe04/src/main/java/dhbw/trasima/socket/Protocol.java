package dhbw.trasima.socket;

import java.util.Locale;

import dhbw.trasima.v2.V2Data;

/**
 * Das TRASIMA-Socket-Protokoll: zeilenbasiertes Request/Response ueber TCP (UTF-8).
 *
 * <pre>
 * Request                                      Response
 * PUT id lat lon speed direction [timestamp]   OK
 * GET id                                       VALUE id lat lon speed direction timestamp | NOT_FOUND
 * DEL id                                       OK | NOT_FOUND
 * LIST                                         VALUES n, gefolgt von n VALUE-Zeilen
 * NEAR lat lon radiusKm                        VALUES n, gefolgt von n VALUE-Zeilen
 * COUNT                                        COUNT n
 * QUIT                                         BYE (Server schliesst die Verbindung)
 * (alles andere)                               ERROR Beschreibung
 * </pre>
 */
public final class Protocol {

    public static final int DEFAULT_PORT = 7001;

    public static final String OK = "OK";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String BYE = "BYE";
    public static final String VALUE = "VALUE";
    public static final String VALUES = "VALUES";
    public static final String COUNT = "COUNT";
    public static final String ERROR = "ERROR";

    private Protocol() {
    }

    /** Fahrzeugdaten als "id lat lon speed direction timestamp". */
    public static String format(V2Data data) {
        return String.format(Locale.ROOT, "%s %.6f %.6f %.2f %.2f %d", data.id(), data.latitude(),
                data.longitude(), data.speed(), data.direction(), data.timestamp());
    }

    /**
     * Liest Fahrzeugdaten aus den Tokens ab Position offset. Fehlt der Zeitstempel,
     * wird die aktuelle Zeit eingesetzt.
     */
    public static V2Data parse(String[] tokens, int offset) {
        int available = tokens.length - offset;
        if (available < 5 || available > 6) {
            throw new IllegalArgumentException("erwartet: id lat lon speed direction [timestamp]");
        }
        long timestamp = available == 6 ? Long.parseLong(tokens[offset + 5]) : System.currentTimeMillis();
        return new V2Data(tokens[offset], Double.parseDouble(tokens[offset + 1]),
                Double.parseDouble(tokens[offset + 2]), Double.parseDouble(tokens[offset + 3]),
                Double.parseDouble(tokens[offset + 4]), timestamp);
    }
}
