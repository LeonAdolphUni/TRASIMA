package dhbw.trasima.v2;

/**
 * Zustand eines Virtual Vehicle (V2) zu einem Zeitpunkt.
 *
 * @param id        eindeutige Kennung des Fahrzeugs (ohne Leerzeichen)
 * @param latitude  Breite in Grad
 * @param longitude Laenge in Grad
 * @param speed     Geschwindigkeit in km/h
 * @param direction Richtung in Grad (Norden = 0, Osten = 90, Sueden = 180, Westen = 270)
 * @param timestamp Zeitpunkt der Messung in Millisekunden seit 1970
 */
public record V2Data(String id, double latitude, double longitude, double speed, double direction,
        long timestamp) {
}
