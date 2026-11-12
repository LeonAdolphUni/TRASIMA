package dhbw.trasima.v2;

/**
 * Aufgabe 02: Simulation ohne Netzwerk. Die V2s fahren durch Mannheim, geben ihre Position
 * auf der Konsole aus und informieren sich ueber ein lokales Informationssystem gegenseitig.
 *
 * Aufruf: SimulationApp [Anzahl V2s]
 */
public class SimulationApp {

    public static void main(String[] args) throws InterruptedException {
        int count = args.length > 0 ? Integer.parseInt(args[0]) : 3;
        LocalInfoSystem infoSystem = new LocalInfoSystem();
        // Dependency Injection: der Publisher wird hier zusammengebaut und den V2s uebergeben
        IPositionPublisher publisher = new ConsolePublisher(infoSystem);
        Fleet.run("V2-", count, publisher, infoSystem);
    }
}
