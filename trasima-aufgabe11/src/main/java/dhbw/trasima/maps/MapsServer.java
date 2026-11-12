package dhbw.trasima.maps;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.servlet.ServletContainer;

import dhbw.trasima.rest.TrasimaRestApplication;
import dhbw.trasima.v2.VehicleStore;

/**
 * Aufgabe 11: REST-Server mit OpenStreetMap-Karte.
 *
 * <pre>
 * /                       Kartenanwendung (HTML/JavaScript mit Leaflet)
 * /api/trasima/vehicles   REST-Dienst aus Aufgabe 10: die V2s melden hier ihre Position,
 *                         die Karte liest hier die Fahrzeugliste (id, lat, lon, speed, dir)
 * </pre>
 *
 * Aufruf: MapsServer [Port]
 */
public class MapsServer {

    public static final int DEFAULT_PORT = 8083;

    /** Startet den Server; Port 0 waehlt einen freien Port (siehe getPort()). */
    public static Server start(int port, VehicleStore store) throws Exception {
        TrasimaRestApplication app = new TrasimaRestApplication(store);
        app.register(CorsFilter.class);

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        context.setBaseResource(Resource.newClassPathResource("/maps"));
        context.setWelcomeFiles(new String[] { "index.html" });
        context.addServlet(new ServletHolder(new ServletContainer(app)), "/api/*");
        context.addServlet(new ServletHolder("default", DefaultServlet.class), "/");
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
        int p = getPort(server);
        System.out.println("TRASIMA Karte:      http://localhost:" + p + "/");
        System.out.println("Fahrzeugliste:      http://localhost:" + p + "/api/trasima/vehicles");
        System.out.println("V2s melden sich an: http://localhost:" + p + "/api  (./run.sh rest-cars 5 V2- http://localhost:"
                + p + "/api)");
        server.join();
    }
}
