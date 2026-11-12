package dhbw.trasima.rest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import dhbw.trasima.v2.Geo;
import dhbw.trasima.v2.IPositionPublisher;
import dhbw.trasima.v2.ITrafficInfo;
import dhbw.trasima.v2.V2Data;

/**
 * REST-Client fuer den TRASIMA-Server (Aufgabe 10): implementiert IPositionPublisher und ITrafficInfo.
 *
 * <ul>
 * <li>erste Meldung eines Fahrzeugs: POST /api/trasima/vehicles/{id} (Create mit Client-Id)</li>
 * <li>weitere Meldungen: PUT /api/trasima/vehicles/{id} (Update)</li>
 * <li>Ziel erreicht: DELETE /api/trasima/vehicles/{id}</li>
 * </ul>
 * Kennt der Server das Fahrzeug nicht mehr (404 bei PUT), wird es neu angelegt; existiert es beim
 * POST schon (409), wird aktualisiert. Fahrzeuge in der Naehe werden aus der Liste aller Fahrzeuge
 * berechnet, so funktioniert der Client mit jedem Server, der die Schnittstelle aus Aufgabe 10 anbietet.
 */
public class RestTrasimaClient implements IPositionPublisher, ITrafficInfo {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final String vehiclesUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private final ObjectMapper json = new ObjectMapper();
    private final Set<String> created = ConcurrentHashMap.newKeySet();

    /**
     * @param baseUrl z.B. http://localhost:8082/api (die Fahrzeuge liegen darunter unter /trasima/vehicles)
     */
    public RestTrasimaClient(String baseUrl) {
        this.vehiclesUrl = baseUrl.replaceAll("/+$", "") + "/trasima/vehicles";
    }

    public String getVehiclesUrl() {
        return vehiclesUrl;
    }

    @Override
    public void publish(V2Data data) {
        String body = toJson(VehicleJson.of(data));
        if (created.contains(data.id())) {
            HttpResponse<String> response = send("PUT", data.id(), body);
            if (response.statusCode() == 404) {        // Server kennt es nicht mehr -> neu anlegen
                created.remove(data.id());
                publish(data);
                return;
            }
            expect(response, 200, 204);
        } else {
            HttpResponse<String> response = send("POST", data.id(), body);
            if (response.statusCode() == 409) {        // existiert schon -> aktualisieren
                expect(send("PUT", data.id(), body), 200, 204);
            } else {
                expect(response, 200, 201, 204);
            }
            created.add(data.id());
        }
    }

    /** Ziel erreicht: Fahrzeug beim Server loeschen. */
    @Override
    public void unpublish(String id) {
        remove(id);
    }

    /** @return die Daten des Fahrzeugs oder null, wenn der Server es nicht kennt */
    public V2Data get(String id) {
        HttpResponse<String> response = send("GET", id, null);
        if (response.statusCode() == 404) {
            return null;
        }
        expect(response, 200);
        return read(response.body(), VehicleJson.class).toV2Data(id, 0);
    }

    /** @return true, wenn das Fahrzeug vorhanden war */
    public boolean remove(String id) {
        created.remove(id);
        HttpResponse<String> response = send("DELETE", id, null);
        if (response.statusCode() == 404) {
            return false;
        }
        expect(response, 200, 204);
        return true;
    }

    @Override
    public List<V2Data> getAll() {
        HttpResponse<String> response = send(request(vehiclesUrl).GET().build());
        expect(response, 200);
        return Arrays.stream(read(response.body(), VehicleJson[].class))
                .map(v -> v.toV2Data(v.id(), 0))
                .toList();
    }

    @Override
    public List<V2Data> getNearby(double latitude, double longitude, double radiusKm) {
        return getAll().stream()
                .filter(v -> Geo.distanceKm(latitude, longitude, v.latitude(), v.longitude()) <= radiusKm)
                .toList();
    }

    private HttpResponse<String> send(String method, String id, String body) {
        HttpRequest.Builder request = request(vehiclesUrl + "/" + encode(id));
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        return send(request.build());
    }

    private static String encode(String id) {
        return URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static HttpRequest.Builder request(String url) {
        return HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).header("Accept", "application/json");
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException("Keine Verbindung zum REST-Server " + vehiclesUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Anfrage an den REST-Server wurde unterbrochen", e);
        }
    }

    private String toJson(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> T read(String body, Class<T> type) {
        try {
            return json.readValue(body, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void expect(HttpResponse<String> response, int... allowed) {
        for (int code : allowed) {
            if (response.statusCode() == code) {
                return;
            }
        }
        throw new IllegalStateException("REST-Server antwortet mit HTTP " + response.statusCode() + ": "
                + response.body());
    }
}
