package dhbw.trasima.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Erzeugt und startet mehrere V2s, die zwischen bekannten Orten in Mannheim fahren.
 *
 * Einstellungen ueber System-Properties:
 *   -Dtrasima.sleep=1000   reale Wartezeit pro Schritt in ms
 *   -Dtrasima.dt=5         simulierte Sekunden pro Schritt
 *   -Dtrasima.route=osrm   Route ueber Internet-Dienst (osrm) oder Luftlinie (direct)
 */
public final class Fleet {

    private Fleet() {
    }

    public static IRouteService defaultRouteService() {
        String kind = System.getProperty("trasima.route", "osrm");
        return "direct".equalsIgnoreCase(kind) ? new DirectRouteService() : new OsrmRouteService();
    }

    public static List<V2> create(String idPrefix, int count, IPositionPublisher publisher,
            ITrafficInfo trafficInfo, IRouteService routeService, Random random) {
        long sleepMillis = Long.getLong("trasima.sleep", 1000);
        double deltaT = Double.parseDouble(System.getProperty("trasima.dt", "5"));
        List<V2> cars = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            int from = random.nextInt(Mannheim.PLACES.length);
            int to = (from + 1 + random.nextInt(Mannheim.PLACES.length - 1)) % Mannheim.PLACES.length;
            double[] start = Mannheim.place(from);
            double[] target = Mannheim.place(to);
            double speed = 40 + random.nextInt(31);
            String id = idPrefix + i;
            List<double[]> route = routeService.route(start[0], start[1], target[0], target[1]);
            V2 car = new V2(id, start[0], start[1], speed, route);
            car.setPublisher(publisher);
            car.setTrafficInfo(trafficInfo);
            car.setTiming(sleepMillis, deltaT);
            cars.add(car);
            System.out.println(id + ": " + Mannheim.placeName(from) + " -> " + Mannheim.placeName(to)
                    + " mit " + (int) speed + " km/h (" + route.size() + " Wegpunkte)");
        }
        return cars;
    }

    /** Startet alle V2s und wartet, bis alle am Ziel sind. */
    public static void startAndJoin(List<V2> cars) throws InterruptedException {
        for (V2 car : cars) {
            car.start();
        }
        for (V2 car : cars) {
            car.join();
        }
    }

    /** Erzeugt count V2s, laesst sie fahren und wartet, bis alle am Ziel sind. */
    public static void run(String idPrefix, int count, IPositionPublisher publisher, ITrafficInfo trafficInfo)
            throws InterruptedException {
        List<V2> cars = create(idPrefix, count, publisher, trafficInfo, defaultRouteService(), new Random());
        startAndJoin(cars);
        System.out.println("Alle " + count + " V2s sind am Ziel.");
    }
}
