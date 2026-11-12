package dhbw.trasima.rest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import dhbw.trasima.v2.VehicleStore;

/**
 * Aufgabe 10: Startet den REST-Server (Jersey in einem eingebetteten Jetty).
 * Die Dienste liegen unter http://localhost:PORT/api/...
 *
 * Aufruf: RestServer [Port]
 */
public class RestServer {

    public static final int DEFAULT_PORT = 8082;

    /** Startet den Server; Port 0 waehlt einen freien Port (siehe getPort()). */
    public static Server start(int port, VehicleStore store) throws Exception {
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        ServletContainer jersey = new ServletContainer(new TrasimaRestApplication(store));
        context.addServlet(new ServletHolder(jersey), "/api/*");
        server.setHandler(context);
        server.start();
        return server;
    }

    public static int getPort(Server server) {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Server server = start(port, new VehicleStore());
        System.out.println("TRASIMA REST Server: http://localhost:" + getPort(server) + "/api/trasima/vehicles");
        server.join();
    }
}
