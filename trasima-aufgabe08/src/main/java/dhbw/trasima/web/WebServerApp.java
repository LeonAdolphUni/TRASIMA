package dhbw.trasima.web;

import dhbw.trasima.grpc.GrpcServerApp;
import dhbw.trasima.grpc.GrpcTrasimaClient;
import dhbw.trasima.v2.ITrafficInfo;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Aufgabe 08: Eingebetteter Jetty-Server. Die Servlets werden nicht ueber einen Deployment
 * Descriptor (web.xml), sondern programmatisch registriert.
 *
 *   /            statische Seite (index.html)
 *   /vehicles    VehicleServlet, holt die Daten per gRPC vom TRASIMA-Server
 *
 * Aufruf: WebServerApp [HTTP-Port] [gRPC-Host] [gRPC-Port]
 */
public class WebServerApp {

    public static final int DEFAULT_PORT = 8081;

    /** Startet den Webserver; Port 0 waehlt einen freien Port (siehe getPort()). */
    public static Server start(int port, ITrafficInfo trafficInfo) throws Exception {
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        context.setBaseResource(Resource.newClassPathResource("/webapp"));
        context.setWelcomeFiles(new String[] { "index.html" });
        context.addServlet(new ServletHolder(new VehicleServlet(trafficInfo)), "/vehicles");
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
        String grpcHost = args.length > 1 ? args[1] : "localhost";
        int grpcPort = args.length > 2 ? Integer.parseInt(args[2]) : GrpcServerApp.DEFAULT_PORT;

        GrpcTrasimaClient client = new GrpcTrasimaClient(grpcHost, grpcPort);
        Server server = start(port, client);
        System.out.println("TRASIMA Webseite: http://localhost:" + getPort(server) + "/vehicles"
                + "  (Daten von gRPC-Server " + grpcHost + ":" + grpcPort + ")");
        server.join();
    }
}
