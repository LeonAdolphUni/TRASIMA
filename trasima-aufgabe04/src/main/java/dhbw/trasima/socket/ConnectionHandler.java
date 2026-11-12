package dhbw.trasima.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Bedient genau eine Client-Verbindung: liest Requests in einer Schleife, verarbeitet sie
 * und schreibt die Antworten zurueck. Pro Verbindung liest und schreibt nur dieser eine Handler.
 * Fehler werden hier lokal behandelt, damit der Server nicht abstuerzt.
 */
public class ConnectionHandler implements Runnable {

    /** Nach so langer Inaktivitaet wird die Verbindung geschlossen (verwaiste Verbindungen). */
    public static final int DEFAULT_TIMEOUT_MILLIS = 60_000;

    private final Socket socket;
    private final CommandProcessor processor;
    private final int timeoutMillis;

    public ConnectionHandler(Socket socket, CommandProcessor processor) {
        this(socket, processor, DEFAULT_TIMEOUT_MILLIS);
    }

    public ConnectionHandler(Socket socket, CommandProcessor processor, int timeoutMillis) {
        this.socket = socket;
        this.processor = processor;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public void run() {
        String client = String.valueOf(socket.getRemoteSocketAddress());
        try (socket;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout(timeoutMillis);
            String line;
            while ((line = reader.readLine()) != null) {
                String response = processor.process(line);
                writer.write(response);
                writer.write('\n');
                writer.flush();
                if (Protocol.BYE.equals(response)) {
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Verbindung zu " + client + " wegen Inaktivitaet geschlossen");
        } catch (IOException e) {
            System.err.println("Verbindung zu " + client + " abgebrochen: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Fehler bei der Verarbeitung fuer " + client + ": " + e);
        }
    }
}
