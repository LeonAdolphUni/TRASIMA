package dhbw.trasima.v2;

import java.util.Locale;

/**
 * Gibt die Position auf der Konsole aus und reicht sie optional an einen weiteren Publisher weiter.
 */
public class ConsolePublisher implements IPositionPublisher {

    private final IPositionPublisher next;

    public ConsolePublisher() {
        this(null);
    }

    public ConsolePublisher(IPositionPublisher next) {
        this.next = next;
    }

    @Override
    public void publish(V2Data data) {
        System.out.println(String.format(Locale.ROOT, "%-8s lat=%.5f lon=%.5f v=%5.1f km/h dir=%5.1f",
                data.id(), data.latitude(), data.longitude(), data.speed(), data.direction()));
        if (next != null) {
            next.publish(data);
        }
    }

    @Override
    public void unpublish(String id) {
        System.out.println(id + ": meldet sich ab");
        if (next != null) {
            next.unpublish(id);
        }
    }
}
