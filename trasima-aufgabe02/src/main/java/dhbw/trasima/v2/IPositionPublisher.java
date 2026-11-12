package dhbw.trasima.v2;

/**
 * Schnittstelle, ueber die ein V2 seinen Standpunkt und seine Geschwindigkeit veroeffentlicht.
 * Die konkrete Implementierung (Konsole, Socket, gRPC, REST, ...) wird dem V2 von aussen
 * uebergeben (Dependency Injection).
 */
public interface IPositionPublisher {

    void publish(V2Data data);

    /**
     * Das Fahrzeug hat sein Ziel erreicht und meldet sich ab (z. B. DELETE beim REST-Server).
     * Standard: nichts tun.
     */
    default void unpublish(String id) {
    }
}
