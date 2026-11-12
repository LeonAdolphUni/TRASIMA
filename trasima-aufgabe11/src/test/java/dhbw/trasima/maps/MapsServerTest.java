package dhbw.trasima.maps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dhbw.trasima.rest.RestTrasimaClient;
import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;

class MapsServerTest {

    private Server server;
    private String baseUrl;

    @BeforeEach
    void startServer() throws Exception {
        server = MapsServer.start(0, new VehicleStore());
        baseUrl = "http://localhost:" + MapsServer.getPort(server);
    }

    @AfterEach
    void stopServer() throws Exception {
        server.stop();
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<Map<String, Object>> vehicles() throws Exception {
        HttpResponse<String> response = get("/api/trasima/vehicles");
        assertEquals(200, response.statusCode());
        assertEquals("*", response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        return new ObjectMapper().readValue(response.body(), new TypeReference<>() { });
    }

    @Test
    void vehiclesPublishedOverRestAppearInMapFormat() throws Exception {
        assertTrue(vehicles().isEmpty());

        RestTrasimaClient client = new RestTrasimaClient(baseUrl + "/api");
        client.publish(new V2Data("V2-2", 49.55, 8.54, 60, 180, 2000));
        client.publish(new V2Data("V2-1", 49.4879, 8.4662, 50, 90, 1000));

        List<Map<String, Object>> list = vehicles();
        assertEquals(2, list.size());
        Map<String, Object> first = list.get(0);
        assertEquals("V2-1", first.get("id"));
        assertEquals(49.4879, ((Number) first.get("lat")).doubleValue(), 1e-9);
        assertEquals(8.4662, ((Number) first.get("lon")).doubleValue(), 1e-9);
        assertEquals(50.0, ((Number) first.get("speed")).doubleValue(), 1e-9);
        assertEquals(90.0, ((Number) first.get("dir")).doubleValue(), 1e-9);

        // Position aendert sich -> derselbe Eintrag mit neuen Koordinaten
        client.publish(new V2Data("V2-1", 49.49, 8.47, 55, 45, 3000));
        assertEquals(49.49, ((Number) vehicles().get(0).get("lat")).doubleValue(), 1e-9);

        // Ziel erreicht -> DELETE -> Fahrzeug verschwindet aus der Liste
        client.unpublish("V2-2");
        assertEquals(List.of("V2-1"), vehicles().stream().map(v -> v.get("id")).toList());
    }

    @Test
    void servesMapPage() throws Exception {
        HttpResponse<String> response = get("/");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("leaflet"));
        assertTrue(response.body().contains("/api/trasima/vehicles"));
    }
}
