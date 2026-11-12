package dhbw.trasima.socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;

class SingleThreadedServerTest {

    private SingleThreadedServer server;
    private VehicleStore store;

    @BeforeEach
    void startServer() throws Exception {
        store = new VehicleStore();
        server = new SingleThreadedServer(0, store);
        Thread thread = new Thread(server::serve);
        thread.setDaemon(true);
        thread.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        server.close();
    }

    @Test
    void clientPublishesAndQueriesOverTheNetwork() {
        try (SocketTrasimaClient client = new SocketTrasimaClient("localhost", server.getPort())) {
            client.publish(new V2Data("V2-1", 49.4879, 8.4662, 50, 90, 1000));
            client.publish(new V2Data("V2-2", 49.5500, 8.5400, 60, 180, 2000));

            assertEquals(2, store.size());
            assertEquals(new V2Data("V2-1", 49.4879, 8.4662, 50, 90, 1000), client.get("V2-1"));
            assertNull(client.get("V2-9"));
            assertEquals(2, client.getAll().size());
            assertEquals(1, client.getNearby(49.4879, 8.4662, 0.5).size());
            assertTrue(client.remove("V2-2"));
            assertEquals(1, client.getAll().size());
        }
    }

    @Test
    void clientsAreServedOneAfterTheOther() {
        try (SocketTrasimaClient first = new SocketTrasimaClient("localhost", server.getPort())) {
            first.publish(new V2Data("A", 49.48, 8.46, 50, 0, 1));
        }
        try (SocketTrasimaClient second = new SocketTrasimaClient("localhost", server.getPort())) {
            assertEquals("A", second.getAll().get(0).id());
        }
    }
}
