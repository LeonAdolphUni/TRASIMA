package dhbw.trasima.rest;

import dhbw.trasima.v2.ConsolePublisher;
import dhbw.trasima.v2.Fleet;

/**
 * Laesst V2s fahren, die ihre Position ueber REST an den TRASIMA-Server melden.
 *
 * Aufruf: RestCarsApp [Anzahl] [Id-Praefix] [Basis-URL]
 *
 * Die Basis-URL endet vor /trasima/vehicles, z. B. http://localhost:8082/api oder fuer den
 * Kursserver http://[2001:7c0:1b20:c913:1::51d]:8080/trasima/api
 * Auf gemeinsam genutzten Servern einen eigenen Praefix waehlen, damit die Ids eindeutig sind.
 */
public class RestCarsApp {

    public static void main(String[] args) throws InterruptedException {
        int count = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        String prefix = args.length > 1 ? args[1] : "V2-";
        String baseUrl = args.length > 2 ? args[2] : "http://localhost:" + RestServer.DEFAULT_PORT + "/api";

        RestTrasimaClient client = new RestTrasimaClient(baseUrl);
        System.out.println("Melde V2s an " + client.getVehiclesUrl());
        Fleet.run(prefix, count, new ConsolePublisher(client), client);
    }
}
