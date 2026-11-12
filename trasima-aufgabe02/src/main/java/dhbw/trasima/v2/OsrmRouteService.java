package dhbw.trasima.v2;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ermittelt die Route ueber den Internet-Dienst OSRM (Strassennetz von OpenStreetMap).
 * Ist der Dienst nicht erreichbar, wird auf die Luftlinie zurueckgefallen.
 */
public class OsrmRouteService implements IRouteService {

    public static final String DEFAULT_BASE_URL = "https://router.project-osrm.org";

    private static final Pattern COORDINATES = Pattern.compile("\"coordinates\":\\[\\[(.*?)\\]\\]");

    private final String baseUrl;
    private final IRouteService fallback = new DirectRouteService();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public OsrmRouteService() {
        this(DEFAULT_BASE_URL);
    }

    public OsrmRouteService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public List<double[]> route(double startLat, double startLon, double targetLat, double targetLon) {
        try {
            String url = String.format(Locale.ROOT,
                    "%s/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                    baseUrl, startLon, startLat, targetLon, targetLat);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "TRASIMA-Uebung")
                    .GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                List<double[]> waypoints = parse(response.body());
                if (!waypoints.isEmpty()) {
                    return waypoints;
                }
            }
            System.err.println("OSRM lieferte keine Route (HTTP " + response.statusCode() + "), verwende Luftlinie");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("OSRM nicht erreichbar (" + e + "), verwende Luftlinie");
        }
        return fallback.route(startLat, startLon, targetLat, targetLon);
    }

    /** Liest die Wegpunkte aus der GeoJSON-Geometrie der OSRM-Antwort ([lon,lat]-Paare). */
    static List<double[]> parse(String json) {
        List<double[]> waypoints = new ArrayList<>();
        Matcher m = COORDINATES.matcher(json);
        if (m.find()) {
            for (String pair : m.group(1).split("\\],\\[")) {
                String[] lonLat = pair.split(",");
                waypoints.add(new double[] { Double.parseDouble(lonLat[1]), Double.parseDouble(lonLat[0]) });
            }
        }
        return waypoints;
    }
}
