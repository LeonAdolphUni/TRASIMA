package dhbw.trasima.socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;

class MultiThreadedServerTest {

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void manyClientsAreServedAtTheSameTime(boolean virtualThreads) throws Exception {
        int clients = 20;
        int messagesPerClient = 50;
        VehicleStore store = new VehicleStore();
        try (MultiThreadedServer server = new MultiThreadedServer(0, store, virtualThreads);
                ExecutorService pool = Executors.newFixedThreadPool(clients)) {
            Thread thread = new Thread(server::serve);
            thread.setDaemon(true);
            thread.start();

            // alle Clients verbinden sich zuerst und senden erst dann: das geht nur,
            // wenn der Server alle Verbindungen gleichzeitig offen halten kann
            CountDownLatch allConnected = new CountDownLatch(clients);
            List<Future<?>> results = new ArrayList<>();
            for (int c = 0; c < clients; c++) {
                String prefix = "C" + c + "-";
                results.add(pool.submit(() -> {
                    try (SocketTrasimaClient client = new SocketTrasimaClient("localhost", server.getPort())) {
                        client.getAll();
                        allConnected.countDown();
                        allConnected.await(10, TimeUnit.SECONDS);
                        for (int i = 0; i < messagesPerClient; i++) {
                            client.publish(new V2Data(prefix + i, 49.48, 8.46, 50, 0, i));
                        }
                    }
                    return null;
                }));
            }
            for (Future<?> result : results) {
                result.get(20, TimeUnit.SECONDS);
            }

            assertEquals(clients * messagesPerClient, store.size());
        }
    }

    @Test
    void brokenClientDoesNotStopTheServer() throws Exception {
        VehicleStore store = new VehicleStore();
        try (MultiThreadedServer server = new MultiThreadedServer(0, store, true)) {
            Thread thread = new Thread(server::serve);
            thread.setDaemon(true);
            thread.start();

            try (java.net.Socket rude = new java.net.Socket("localhost", server.getPort())) {
                rude.getOutputStream().write("PUT kaputt\nUNSINN\n".getBytes());
                rude.setSoLinger(true, 0); // Verbindung hart abbrechen
            }
            try (SocketTrasimaClient client = new SocketTrasimaClient("localhost", server.getPort())) {
                client.publish(new V2Data("V2-1", 49.48, 8.46, 50, 0, 1));
                assertEquals(1, client.getAll().size());
            }
        }
    }
}
