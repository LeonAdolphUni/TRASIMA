package dhbw.trasima.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dhbw.trasima.v2.ITrafficInfo;
import dhbw.trasima.v2.Mannheim;
import dhbw.trasima.v2.V2Data;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Aufgabe 08: Stellt die Daten aller Fahrzeuge des TRASIMA-Servers als einfache Webseite dar
 * (Tabelle und Lageplan). Die Seite aktualisiert sich selbst; mit ?refresh=0 wird das abgeschaltet,
 * mit ?refresh=5 auf 5 Sekunden gestellt.
 */
public class VehicleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int MAP_WIDTH = 410;
    private static final int MAP_HEIGHT = 500;

    private final transient ITrafficInfo trafficInfo;

    /**
     * @param trafficInfo Quelle der Fahrzeugdaten, im Gesamtsystem der gRPC-Client
     */
    public VehicleServlet(ITrafficInfo trafficInfo) {
        this.trafficInfo = trafficInfo;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        List<V2Data> vehicles;
        try {
            vehicles = trafficInfo.getAll();
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            PrintWriter out = response.getWriter();
            writeHead(out, 5);
            out.println("<h1>TRASIMA &ndash; Fahrzeuge</h1>");
            out.println("<p class=\"error\">Der TRASIMA-Server ist nicht erreichbar: " + escape(e.getMessage()) + "</p>");
            out.println("</body></html>");
            return;
        }

        PrintWriter out = response.getWriter();
        writeHead(out, refreshSeconds(request));
        out.println("<h1>TRASIMA &ndash; Fahrzeuge</h1>");
        long driving = vehicles.stream().filter(v -> v.speed() > 0).count();
        out.println("<p>" + vehicles.size() + " Fahrzeuge, davon " + driving + " unterwegs. Stand: "
                + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "</p>");
        out.println("<div class=\"layout\">");
        writeTable(out, vehicles);
        writeMap(out, vehicles);
        out.println("</div>");
        out.println("</body></html>");
    }

    private static int refreshSeconds(HttpServletRequest request) {
        try {
            String value = request.getParameter("refresh");
            return value == null ? 2 : Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    private static void writeHead(PrintWriter out, int refreshSeconds) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"de\"><head><meta charset=\"UTF-8\">");
        if (refreshSeconds > 0) {
            out.println("<meta http-equiv=\"refresh\" content=\"" + refreshSeconds + "\">");
        }
        out.println("<title>TRASIMA - Fahrzeuge</title>");
        out.println("<style>");
        out.println("body { font-family: sans-serif; margin: 2rem; }");
        out.println(".layout { display: flex; flex-wrap: wrap; gap: 2rem; align-items: flex-start; }");
        out.println("table { border-collapse: collapse; }");
        out.println("th, td { border: 1px solid #999; padding: 0.3rem 0.7rem; text-align: right; }");
        out.println("th { background: #eee; }");
        out.println("td:first-child, th:first-child { text-align: left; }");
        out.println("tr.parked { color: #888; }");
        out.println("svg { border: 1px solid #999; background: #f4f7f4; }");
        out.println(".error { color: #b00020; }");
        out.println("</style></head><body>");
    }

    private static void writeTable(PrintWriter out, List<V2Data> vehicles) {
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        out.println("<table>");
        out.println("<tr><th>Id</th><th>Breite</th><th>L&auml;nge</th><th>km/h</th><th>Richtung</th>"
                + "<th>Status</th><th>Letzte Meldung</th></tr>");
        for (V2Data v : vehicles) {
            boolean driving = v.speed() > 0;
            out.println(String.format(Locale.ROOT,
                    "<tr%s><td>%s</td><td>%.5f</td><td>%.5f</td><td>%.1f</td><td>%.0f&deg;</td><td>%s</td><td>%s</td></tr>",
                    driving ? "" : " class=\"parked\"", escape(v.id()), v.latitude(), v.longitude(), v.speed(),
                    v.direction(), driving ? "f&auml;hrt" : "steht", time.format(new Date(v.timestamp()))));
        }
        if (vehicles.isEmpty()) {
            out.println("<tr><td colspan=\"7\">Noch keine Fahrzeuge gemeldet.</td></tr>");
        }
        out.println("</table>");
    }

    /** Lageplan: das Gebiet Mannheim als Rechteck, jedes Fahrzeug als Punkt. */
    private static void writeMap(PrintWriter out, List<V2Data> vehicles) {
        out.println("<svg width=\"" + MAP_WIDTH + "\" height=\"" + MAP_HEIGHT + "\" viewBox=\"0 0 " + MAP_WIDTH
                + " " + MAP_HEIGHT + "\" role=\"img\" aria-label=\"Lageplan Mannheim\">");
        for (int i = 0; i < Mannheim.PLACES.length; i++) {
            double[] p = Mannheim.place(i);
            out.println(String.format(Locale.ROOT,
                    "<rect x=\"%.1f\" y=\"%.1f\" width=\"5\" height=\"5\" fill=\"#999\"/>",
                    x(p[1]) - 2.5, y(p[0]) - 2.5));
            out.println(label(x(p[1]), y(p[0]) + 12, 9, "#777", Mannheim.placeName(i)));
        }
        for (V2Data v : vehicles) {
            if (!Mannheim.contains(v.latitude(), v.longitude())) {
                continue;
            }
            out.println(String.format(Locale.ROOT, "<circle cx=\"%.1f\" cy=\"%.1f\" r=\"5\" fill=\"%s\"/>",
                    x(v.longitude()), y(v.latitude()), v.speed() > 0 ? "#1565c0" : "#888"));
            out.println(label(x(v.longitude()), y(v.latitude()) - 4, 10, "#000", v.id()));
        }
        out.println("</svg>");
    }

    /** Beschriftung rechts vom Punkt; nahe am rechten Rand links davon, damit nichts abgeschnitten wird. */
    private static String label(double x, double y, int fontSize, String color, String text) {
        boolean nearRightEdge = x > MAP_WIDTH * 0.75;
        return String.format(Locale.ROOT,
                "<text x=\"%.1f\" y=\"%.1f\" font-size=\"%d\" fill=\"%s\" text-anchor=\"%s\">%s</text>",
                nearRightEdge ? x - 7 : x + 7, y, fontSize, color, nearRightEdge ? "end" : "start", escape(text));
    }

    private static double x(double longitude) {
        return (longitude - Mannheim.MIN_LON) / (Mannheim.MAX_LON - Mannheim.MIN_LON) * MAP_WIDTH;
    }

    private static double y(double latitude) {
        return (Mannheim.MAX_LAT - latitude) / (Mannheim.MAX_LAT - Mannheim.MIN_LAT) * MAP_HEIGHT;
    }

    static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
