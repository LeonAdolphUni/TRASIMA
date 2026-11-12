package dhbw.trasima.v2;

import java.util.List;
import java.util.Locale;

/**
 * Ein Virtual Vehicle (V2): faehrt autonom entlang einer Route vom Start zum Ziel.
 * In jedem Zeitschritt veroeffentlicht es seine Position, informiert sich ueber andere V2s
 * in der Naehe, passt seine Geschwindigkeit an und faehrt ein Stueck weiter.
 *
 * Publisher und Verkehrsinformation werden von aussen gesetzt (Dependency Injection),
 * damit das V2 nicht weiss, ob es mit der Konsole, einem Socket-, gRPC- oder REST-Server spricht.
 */
public class V2 extends Thread {

    /** Kommen sich zwei V2s naeher als dieser Abstand, bremst eines von beiden. */
    public static final double SAFETY_DISTANCE_KM = 0.1;

    /** Umkreis, in dem sich ein V2 ueber andere V2s informiert. */
    public static final double LOOKUP_RADIUS_KM = 0.3;

    /** Meldungen, die aelter sind, werden ignoriert (in ms). */
    private static final long MAX_AGE_MILLIS = 60_000;

    private final String id;
    private final List<double[]> route;
    private final double desiredSpeed;

    private double latitude;
    private double longitude;
    private double speed;
    private double direction;
    private int nextWaypoint = 0;
    private volatile boolean isDriving = true;

    /** Reale Wartezeit zwischen zwei Schritten in ms. */
    private long sleepMillis = 1000;
    /** Simulierte Zeit pro Schritt in Sekunden (Euler-Naeherung: kleiner als 10 s). */
    private double deltaT = 5.0;

    private IPositionPublisher publisher;
    private ITrafficInfo trafficInfo;
    private List<V2Data> others = List.of();
    private boolean publishFailed = false;

    /**
     * @param route Wegpunkte als {latitude, longitude}; der letzte Wegpunkt ist das Ziel
     */
    public V2(String id, double startLatitude, double startLongitude, double speedKmh, List<double[]> route) {
        super("V2-Thread-" + id);
        if (speedKmh <= 0) {
            throw new IllegalArgumentException("Die Geschwindigkeit muss groesser als 0 sein");
        }
        if (route.isEmpty()) {
            throw new IllegalArgumentException("Die Route braucht mindestens einen Wegpunkt");
        }
        this.id = id;
        this.latitude = startLatitude;
        this.longitude = startLongitude;
        this.desiredSpeed = speedKmh;
        this.speed = speedKmh;
        this.route = route;
        double[] first = route.get(0);
        this.direction = Geo.bearing(latitude, longitude, first[0], first[1]);
    }

    public void setPublisher(IPositionPublisher publisher) {
        this.publisher = publisher;
    }

    public void setTrafficInfo(ITrafficInfo trafficInfo) {
        this.trafficInfo = trafficInfo;
    }

    /**
     * @param sleepMillis   reale Wartezeit pro Schritt in ms
     * @param deltaTSeconds simulierte Zeit pro Schritt in Sekunden
     */
    public void setTiming(long sleepMillis, double deltaTSeconds) {
        this.sleepMillis = sleepMillis;
        this.deltaT = deltaTSeconds;
    }

    @Override
    public void run() {
        while (isDriving) {
            try {
                sleep(sleepMillis);
                this.publishPosition();
                this.lookForOtherCars();
                this.adjustMySpeed();
                this.followRoute();
                this.drive();
                // ueberpruefen ob am Ziel angekommen
                if (nextWaypoint >= route.size()) {
                    this.isDriving = false;
                    this.speed = 0;
                    this.publishPosition();
                    System.out.println(id + ": stop the car as it reaches its target");
                    this.unpublish();
                }
            } catch (InterruptedException e) {
                this.isDriving = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Haelt das Fahrzeug an. */
    public void stopDriving() {
        this.isDriving = false;
        this.interrupt();
    }

    private void publishPosition() {
        if (publisher == null) {
            return;
        }
        try {
            publisher.publish(getData());
            publishFailed = false;
        } catch (RuntimeException e) {
            if (!publishFailed) {
                System.err.println(id + ": Position konnte nicht veroeffentlicht werden: " + e.getMessage());
            }
            publishFailed = true;
        }
    }

    /** Am Ziel: beim Informationssystem abmelden. */
    private void unpublish() {
        if (publisher == null) {
            return;
        }
        try {
            publisher.unpublish(id);
        } catch (RuntimeException e) {
            System.err.println(id + ": Abmelden fehlgeschlagen: " + e.getMessage());
        }
    }

    private void lookForOtherCars() {
        if (trafficInfo == null) {
            others = List.of();
            return;
        }
        try {
            others = trafficInfo.getNearby(latitude, longitude, LOOKUP_RADIUS_KM);
        } catch (RuntimeException e) {
            others = List.of();
        }
    }

    /**
     * Kommt ein anderes fahrendes V2 zu nahe, bremst das V2 mit der groesseren Id ab
     * (einfache Vorfahrtsregel), bis der Abstand wieder gross genug ist.
     */
    private void adjustMySpeed() {
        long now = System.currentTimeMillis();
        boolean mustYield = false;
        for (V2Data other : others) {
            if (other.id().equals(id) || other.speed() <= 0 || now - other.timestamp() > MAX_AGE_MILLIS) {
                continue;
            }
            double distance = Geo.distanceKm(latitude, longitude, other.latitude(), other.longitude());
            if (distance < SAFETY_DISTANCE_KM && id.compareTo(other.id()) > 0) {
                mustYield = true;
                if (speed == desiredSpeed) {
                    System.out.println(String.format(Locale.ROOT, "%s: %s ist nur %.0f m entfernt, bremse ab",
                            id, other.id(), distance * 1000));
                }
            }
        }
        if (!mustYield && speed != desiredSpeed) {
            System.out.println(id + ": Strecke frei, beschleunige wieder");
        }
        speed = mustYield ? desiredSpeed * 0.3 : desiredSpeed;
    }

    /** Richtet das Fahrzeug auf den naechsten Wegpunkt der Route aus. */
    private void followRoute() {
        while (nextWaypoint < route.size()) {
            double[] waypoint = route.get(nextWaypoint);
            if (Geo.distanceKm(latitude, longitude, waypoint[0], waypoint[1]) < 0.001) {
                nextWaypoint++;
            } else {
                direction = Geo.bearing(latitude, longitude, waypoint[0], waypoint[1]);
                return;
            }
        }
    }

    /** Faehrt deltaT Sekunden lang mit der aktuellen Geschwindigkeit entlang der Route. */
    private void drive() {
        double remainingKm = speed * deltaT / 3600.0;
        while (remainingKm > 0 && nextWaypoint < route.size()) {
            double[] waypoint = route.get(nextWaypoint);
            double distance = Geo.distanceKm(latitude, longitude, waypoint[0], waypoint[1]);
            if (distance <= remainingKm) {
                // Wegpunkt wird in diesem Schritt erreicht
                latitude = waypoint[0];
                longitude = waypoint[1];
                remainingKm -= distance;
                nextWaypoint++;
            } else {
                direction = Geo.bearing(latitude, longitude, waypoint[0], waypoint[1]);
                double seconds = remainingKm / speed * 3600.0;
                double[] position = Geo.move(latitude, longitude, speed, direction, seconds);
                latitude = position[0];
                longitude = position[1];
                remainingKm = 0;
            }
        }
    }

    public V2Data getData() {
        return new V2Data(id, latitude, longitude, speed, direction, System.currentTimeMillis());
    }

    public String getVehicleId() {
        return id;
    }

    public boolean isDriving() {
        return isDriving;
    }
}
