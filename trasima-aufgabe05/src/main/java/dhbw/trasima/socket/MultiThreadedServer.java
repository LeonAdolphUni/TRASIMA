package dhbw.trasima.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dhbw.trasima.v2.VehicleStore;

/**
 * Aufgabe 05: Nebenlaeufiger (Multi-Threaded) Server. Der Haupt-Thread nimmt nur Verbindungen an
 * (Dispatcher) und gibt jede Verbindung an einen eigenen Thread weiter (Worker), der sie mit einem
 * ConnectionHandler bedient. So werden viele Clients gleichzeitig bedient.
 *
 * Standard sind die Virtual Threads von Java 21 ("1 Connection = 1 Thread", ohne Thread-Pool).
 * Zum Vergleich kann ein klassischer Thread-Pool mit Betriebssystem-Threads gewaehlt werden.
 *
 * Aufruf: MultiThreadedServer [Port] [virtual|platform]
 */
public class MultiThreadedServer implements Closeable {

    /** Groesse des Thread-Pools in der Variante mit Betriebssystem-Threads. */
    public static final int PLATFORM_POOL_SIZE = 50;

    private final ServerSocket serverSocket;
    private final CommandProcessor processor;
    private final ExecutorService executor;

    public MultiThreadedServer(int port, VehicleStore store, boolean virtualThreads) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.processor = new CommandProcessor(store);
        this.executor = virtualThreads
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(PLATFORM_POOL_SIZE);
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /** Nimmt Verbindungen an, bis der Server geschlossen wird. */
    public void serve() {
        while (!serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept();
                executor.submit(new ConnectionHandler(client, processor));
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Fehler beim Annehmen einer Verbindung: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
        executor.shutdownNow();
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Protocol.DEFAULT_PORT;
        boolean virtualThreads = args.length < 2 || !"platform".equalsIgnoreCase(args[1]);
        try (MultiThreadedServer server = new MultiThreadedServer(port, new VehicleStore(), virtualThreads)) {
            System.out.println("TRASIMA Multi-Threaded Server lauscht auf Port " + server.getPort()
                    + (virtualThreads ? " (Virtual Threads)" : " (Thread-Pool mit " + PLATFORM_POOL_SIZE + " Threads)"));
            server.serve();
        }
    }
}
