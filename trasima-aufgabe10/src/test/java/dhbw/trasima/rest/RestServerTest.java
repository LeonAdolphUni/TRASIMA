package dhbw.trasima.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dhbw.trasima.v2.Mannheim;
import dhbw.trasima.v2.V2;
import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;

class RestServerTest {

    private static final String BODY = "{\"lat\":49.48,\"lon\":8.46,\"speed\":50,\"dir\":90}";

    private Server server;
    private VehicleStore store;
    private String baseUrl;
    private RestTrasimaClient client;

    @BeforeEach
    void startServer() throws Exception {
        store = new VehicleStore();
        server = RestServer.start(0, store);
        baseUrl = "http://localhost:" + RestServer.getPort(server) + "/api";
        client = new RestTrasimaClient(baseUrl);
    }

    @AfterEach
    void stopServer() throws Exception {
        server.stop();
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + "/trasima/vehicles" + path));
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void crudFollowsTheSpecification() throws Exception {
        assertEquals("[]", send("GET", "", null).body());

        HttpResponse<String> created = send("POST", "/4711", BODY);
        assertEquals(201, created.statusCode());
        assertTrue(created.headers().firstValue("Location").orElseThrow().endsWith("/trasima/vehicles/4711"));
        assertEquals(409, send("POST", "/4711", BODY).statusCode(), "Id existiert bereits");

        String one = send("GET", "/4711", null).body();
        assertTrue(one.contains("\"id\":\"4711\"") && one.contains("\"lat\":49.48") && one.contains("\"lon\":8.46")
                && one.contains("\"speed\":50.0") && one.contains("\"dir\":90.0"), one);

        assertEquals(200, send("PUT", "/4711", BODY.replace("50", "30")).statusCode());
        assertEquals(30.0, store.get("4711").speed());
        assertEquals(404, send("PUT", "/nobody", BODY).statusCode(), "Update nur fuer vorhandene Fahrzeuge");

        assertTrue(send("GET", "", null).body().contains("\"id\":\"4711\""));
        assertEquals(204, send("DELETE", "/4711", null).statusCode());
        assertEquals(404, send("DELETE", "/4711", null).statusCode());
        assertEquals(404, send("GET", "/4711", null).statusCode());
    }

    @Test
    void errorsUseHttpStatusCodes() throws Exception {
        assertEquals(400, send("POST", "/1", "{kaputt").statusCode());
        assertTrue(send("GET", "/nobody", null).body().contains("\"error\""));
    }

    @Test
    void clientCreatesUpdatesAndDeletes() {
        V2Data first = new V2Data("V2-1", 49.4879, 8.4662, 50, 90, 0);
        client.publish(first);                                           // POST
        client.publish(new V2Data("V2-1", 49.4880, 8.4663, 55, 80, 0));  // PUT
        client.publish(new V2Data("V2-2", 49.5500, 8.5400, 60, 180, 0));

        assertEquals(2, store.size());
        assertEquals(55.0, client.get("V2-1").speed());
        assertNull(client.get("V2-9"));
        assertEquals(2, client.getAll().size());
        assertEquals(List.of("V2-1"), client.getNearby(49.4879, 8.4662, 0.5).stream().map(V2Data::id).toList());

        store.remove("V2-1");                 // Server hat es "vergessen" -> PUT 404 -> neu anlegen
        client.publish(first);
        assertEquals(50.0, store.get("V2-1").speed());

        RestTrasimaClient other = new RestTrasimaClient(baseUrl);
        other.publish(new V2Data("V2-2", 49.0, 8.0, 10, 0, 0));  // POST 409 -> PUT
        assertEquals(10.0, store.get("V2-2").speed());

        assertTrue(client.remove("V2-2"));
        assertFalse(client.remove("V2-2"));
        assertEquals(1, client.getAll().size());
    }

    @Test
    void carDrivesReportsAndIsDeletedAtTarget() throws Exception {
        double[] start = Mannheim.place(1);
        double[] target = Mannheim.place(2);
        V2 car = new V2("R-1", start[0], start[1], 50, List.of(target));
        car.setPublisher(client);
        car.setTrafficInfo(client);
        car.setTiming(1, 5);
        car.start();
        car.join(15_000);

        assertFalse(car.isAlive());
        assertNull(store.get("R-1"), "am Ziel meldet sich das V2 per DELETE ab");
    }
}
