package dhbw.trasima.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import dhbw.trasima.grpc.GrpcServerApp;
import dhbw.trasima.grpc.GrpcTrasimaClient;
import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;

class WebServerTest {

    private io.grpc.Server grpcServer;
    private GrpcTrasimaClient grpcClient;
    private Server webServer;

    @AfterEach
    void stop() throws Exception {
        if (webServer != null) {
            webServer.stop();
        }
        if (grpcClient != null) {
            grpcClient.close();
        }
        if (grpcServer != null) {
            grpcServer.shutdownNow();
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        URI uri = URI.create("http://localhost:" + WebServerApp.getPort(webServer) + path);
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void pageShowsTheVehiclesOfTheGrpcServer() throws Exception {
        VehicleStore store = new VehicleStore();
        store.put(new V2Data("V2-1", 49.4879, 8.4662, 50, 90, System.currentTimeMillis()));
        store.put(new V2Data("<b>frech</b>", 49.4800, 8.4700, 0, 0, System.currentTimeMillis()));
        grpcServer = GrpcServerApp.start(0, store);
        grpcClient = new GrpcTrasimaClient("localhost", grpcServer.getPort());
        webServer = WebServerApp.start(0, grpcClient);

        HttpResponse<String> response = get("/vehicles");

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
        assertTrue(response.body().contains("V2-1"));
        assertTrue(response.body().contains("49.48790"));
        assertTrue(response.body().contains("2 Fahrzeuge, davon 1 unterwegs"));
        assertTrue(response.body().contains("&lt;b&gt;frech&lt;/b&gt;"));
        assertFalse(response.body().contains("<b>frech</b>"));
        assertTrue(response.body().contains("http-equiv=\"refresh\""));
        assertFalse(get("/vehicles?refresh=0").body().contains("http-equiv=\"refresh\""));
    }

    @Test
    void staticStartPageIsServed() throws Exception {
        grpcServer = GrpcServerApp.start(0, new VehicleStore());
        grpcClient = new GrpcTrasimaClient("localhost", grpcServer.getPort());
        webServer = WebServerApp.start(0, grpcClient);

        HttpResponse<String> response = get("/");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Traffic Simulation Mannheim"));
    }

    @Test
    void pageReportsWhenTheGrpcServerIsDown() throws Exception {
        grpcServer = GrpcServerApp.start(0, new VehicleStore());
        grpcClient = new GrpcTrasimaClient("localhost", grpcServer.getPort());
        webServer = WebServerApp.start(0, grpcClient);
        grpcServer.shutdownNow();
        grpcServer.awaitTermination();

        HttpResponse<String> response = get("/vehicles");

        assertEquals(503, response.statusCode());
        assertTrue(response.body().contains("nicht erreichbar"));
    }
}
