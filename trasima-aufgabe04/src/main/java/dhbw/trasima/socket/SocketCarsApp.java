package dhbw.trasima.socket;

import dhbw.trasima.v2.ConsolePublisher;
import dhbw.trasima.v2.Fleet;

/**
 * Laesst V2s fahren, die ihre Position ueber Sockets an den TRASIMA-Server melden
 * (Single- oder Multi-Threaded Server).
 *
 * Aufruf: SocketCarsApp [Anzahl] [Id-Praefix] [Host] [Port]
 */
public class SocketCarsApp {

    public static void main(String[] args) throws InterruptedException {
        int count = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        String prefix = args.length > 1 ? args[1] : "V2-";
        String host = args.length > 2 ? args[2] : "localhost";
        int port = args.length > 3 ? Integer.parseInt(args[3]) : Protocol.DEFAULT_PORT;

        try (SocketTrasimaClient client = new SocketTrasimaClient(host, port)) {
            Fleet.run(prefix, count, new ConsolePublisher(client), client);
        }
    }
}
