package dhbw.trasima.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import dhbw.trasima.v2.VehicleStore;

/**
 * Aufgabe 04: Iterativer (Single-Threaded) Server. Nimmt eine Verbindung an und bedient sie
 * vollstaendig, bevor die naechste Verbindung angenommen wird. Weitere Clients muessen so lange warten.
 *
 * Aufruf: SingleThreadedServer [Port]
 */
public class SingleThreadedServer implements Closeable {

    private final ServerSocket serverSocket;
    private final CommandProcessor processor;

    public SingleThreadedServer(int port, VehicleStore store) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.processor = new CommandProcessor(store);
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /** Bedient Clients nacheinander, bis der Server geschlossen wird. */
    public void serve() {
        while (!serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept();
                new ConnectionHandler(client, processor).run();
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
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Protocol.DEFAULT_PORT;
        try (SingleThreadedServer server = new SingleThreadedServer(port, new VehicleStore())) {
            System.out.println("TRASIMA Single-Threaded Server lauscht auf Port " + server.getPort());
            server.serve();
        }
    }
}
