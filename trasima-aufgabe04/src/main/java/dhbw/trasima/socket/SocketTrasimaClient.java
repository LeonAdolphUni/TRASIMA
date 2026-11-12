package dhbw.trasima.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dhbw.trasima.v2.IPositionPublisher;
import dhbw.trasima.v2.ITrafficInfo;
import dhbw.trasima.v2.V2Data;

/**
 * Client fuer den TRASIMA-Socket-Server. Haelt eine Verbindung offen und verwendet sie fuer alle
 * Anfragen. Die Methoden sind synchronisiert, damit mehrere V2-Threads denselben Client teilen koennen
 * (pro Socket darf immer nur ein Request/Response-Paar unterwegs sein).
 */
public class SocketTrasimaClient implements IPositionPublisher, ITrafficInfo, Closeable {

    private static final int TIMEOUT_MILLIS = 5_000;

    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public SocketTrasimaClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public synchronized void publish(V2Data data) {
        String response = request("PUT " + Protocol.format(data));
        if (!Protocol.OK.equals(response)) {
            throw new IllegalStateException("Server antwortet: " + response);
        }
    }

    /** @return die Daten des Fahrzeugs oder null, wenn der Server es nicht kennt */
    public synchronized V2Data get(String id) {
        String response = request("GET " + id);
        if (Protocol.NOT_FOUND.equals(response)) {
            return null;
        }
        return parseValue(response);
    }

    /** @return true, wenn das Fahrzeug vorhanden war */
    public synchronized boolean remove(String id) {
        return Protocol.OK.equals(request("DEL " + id));
    }

    @Override
    public synchronized List<V2Data> getAll() {
        return readList(request("LIST"));
    }

    @Override
    public synchronized List<V2Data> getNearby(double latitude, double longitude, double radiusKm) {
        return readList(request(String.format(Locale.ROOT, "NEAR %.6f %.6f %.4f", latitude, longitude, radiusKm)));
    }

    /** Sendet eine Zeile und liefert die erste Antwortzeile. */
    private String request(String line) {
        try {
            connect();
            writer.write(line);
            writer.write('\n');
            writer.flush();
            return readLine();
        } catch (IOException e) {
            disconnect();
            throw new UncheckedIOException("Keine Verbindung zum Server " + host + ":" + port, e);
        }
    }

    private List<V2Data> readList(String header) {
        String[] tokens = header.split(" ");
        if (tokens.length != 2 || !Protocol.VALUES.equals(tokens[0])) {
            throw new IllegalStateException("Server antwortet: " + header);
        }
        try {
            int count = Integer.parseInt(tokens[1]);
            List<V2Data> result = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                result.add(parseValue(readLine()));
            }
            return result;
        } catch (IOException e) {
            disconnect();
            throw new UncheckedIOException("Verbindung zum Server " + host + ":" + port + " abgebrochen", e);
        }
    }

    private String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Server hat die Verbindung geschlossen");
        }
        return line;
    }

    private static V2Data parseValue(String line) {
        String[] tokens = line.split(" ");
        if (!Protocol.VALUE.equals(tokens[0])) {
            throw new IllegalStateException("Server antwortet: " + line);
        }
        return Protocol.parse(tokens, 1);
    }

    private void connect() throws IOException {
        if (socket != null) {
            return;
        }
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, port), TIMEOUT_MILLIS);
        s.setSoTimeout(TIMEOUT_MILLIS);
        socket = s;
        reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
    }

    private void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // die Verbindung ist ohnehin nicht mehr brauchbar
            }
            socket = null;
        }
    }

    @Override
    public synchronized void close() {
        if (socket != null) {
            try {
                writer.write("QUIT\n");
                writer.flush();
            } catch (IOException ignored) {
                // Server ist schon weg
            }
            disconnect();
        }
    }
}
