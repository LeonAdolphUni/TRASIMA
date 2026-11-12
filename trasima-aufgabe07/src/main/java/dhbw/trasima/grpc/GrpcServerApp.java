package dhbw.trasima.grpc;

import java.io.IOException;

import dhbw.trasima.v2.VehicleStore;
import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Aufgabe 07: Der TRASIMA-Server als gRPC-Dienst (ersetzt den Socket-Server).
 *
 * Aufruf: GrpcServerApp [Port]
 */
public class GrpcServerApp {

    public static final int DEFAULT_PORT = 50051;

    /** Startet den Server; Port 0 waehlt einen freien Port (siehe Server.getPort()). */
    public static Server start(int port, VehicleStore store) throws IOException {
        return ServerBuilder.forPort(port)
                .addService(new TrasimaServiceImpl(store))
                .build()
                .start();
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Server server = start(port, new VehicleStore());
        System.out.println("TRASIMA gRPC Server lauscht auf Port " + server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }
}
