package dhbw.trasima.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dhbw.trasima.v2.Mannheim;
import dhbw.trasima.v2.V2;
import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;

class GrpcServerTest {

    private Server server;
    private VehicleStore store;
    private GrpcTrasimaClient client;

    @BeforeEach
    void startServer() throws Exception {
        store = new VehicleStore();
        server = GrpcServerApp.start(0, store);
        client = new GrpcTrasimaClient("localhost", server.getPort());
    }

    @AfterEach
    void stopServer() {
        client.close();
        server.shutdownNow();
    }

    @Test
    void clientPublishesAndQueriesOverGrpc() {
        V2Data first = new V2Data("V2-1", 49.4879, 8.4662, 50, 90, 1000);
        client.publish(first);
        client.publish(new V2Data("V2-2", 49.5500, 8.5400, 60, 180, 2000));

        assertEquals(2, store.size());
        assertEquals(first, client.get("V2-1"));
        assertNull(client.get("V2-9"));
        assertEquals(2, client.getAll().size());
        assertEquals(List.of(first), client.getNearby(49.4879, 8.4662, 0.5));
        assertTrue(client.remove("V2-2"));
        assertFalse(client.remove("V2-2"));
        assertEquals(1, client.getAll().size());
    }

    @Test
    void vehicleWithoutIdIsRejected() {
        assertThrows(StatusRuntimeException.class,
                () -> client.publish(new V2Data("", 49.48, 8.46, 50, 0, 1)));
        assertEquals(0, store.size());
    }

    @Test
    void carDrivesAndReportsToTheGrpcServer() throws Exception {
        double[] start = Mannheim.place(1);
        double[] target = Mannheim.place(2);
        V2 car = new V2("G-1", start[0], start[1], 50, List.of(target));
        car.setPublisher(client);
        car.setTrafficInfo(client);
        car.setTiming(1, 5);
        car.start();
        car.join(15_000);

        V2Data last = store.get("G-1");
        assertFalse(car.isAlive());
        assertEquals(0.0, last.speed());
        assertEquals(target[0], last.latitude(), 1e-9);
        assertEquals(target[1], last.longitude(), 1e-9);
    }
}
