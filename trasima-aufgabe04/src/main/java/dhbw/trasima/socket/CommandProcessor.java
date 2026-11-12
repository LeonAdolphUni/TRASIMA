package dhbw.trasima.socket;

import java.util.List;

import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;

/**
 * Parsing und Business-Logik des Protokolls: wandelt eine Request-Zeile in die Antwort um.
 * Ist unabhaengig von Sockets und Threads und damit fuer beide Server verwendbar.
 */
public class CommandProcessor {

    private final VehicleStore store;

    public CommandProcessor(VehicleStore store) {
        this.store = store;
    }

    /**
     * @return die Antwort ohne abschliessenden Zeilenumbruch (bei Listen mehrere Zeilen)
     */
    public String process(String line) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return Protocol.ERROR + " leere Anfrage";
        }
        try {
            switch (tokens[0].toUpperCase()) {
                case "PUT":
                    store.put(Protocol.parse(tokens, 1));
                    return Protocol.OK;
                case "GET": {
                    requireArguments(tokens, 1, "GET id");
                    V2Data data = store.get(tokens[1]);
                    return data == null ? Protocol.NOT_FOUND : Protocol.VALUE + " " + Protocol.format(data);
                }
                case "DEL":
                    requireArguments(tokens, 1, "DEL id");
                    return store.remove(tokens[1]) ? Protocol.OK : Protocol.NOT_FOUND;
                case "LIST":
                    requireArguments(tokens, 0, "LIST");
                    return list(store.getAll());
                case "NEAR":
                    requireArguments(tokens, 3, "NEAR lat lon radiusKm");
                    return list(store.getNearby(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]),
                            Double.parseDouble(tokens[3])));
                case "COUNT":
                    requireArguments(tokens, 0, "COUNT");
                    return Protocol.COUNT + " " + store.size();
                case "QUIT":
                    return Protocol.BYE;
                default:
                    return Protocol.ERROR + " unbekanntes Kommando " + tokens[0];
            }
        } catch (NumberFormatException e) {
            return Protocol.ERROR + " ungueltige Zahl";
        } catch (IllegalArgumentException e) {
            return Protocol.ERROR + " " + e.getMessage();
        }
    }

    private static void requireArguments(String[] tokens, int count, String usage) {
        if (tokens.length != count + 1) {
            throw new IllegalArgumentException("erwartet: " + usage);
        }
    }

    private static String list(List<V2Data> vehicles) {
        StringBuilder response = new StringBuilder(Protocol.VALUES + " " + vehicles.size());
        for (V2Data data : vehicles) {
            response.append('\n').append(Protocol.VALUE).append(' ').append(Protocol.format(data));
        }
        return response.toString();
    }
}
