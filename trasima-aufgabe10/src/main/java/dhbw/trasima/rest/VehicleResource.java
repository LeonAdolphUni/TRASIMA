package dhbw.trasima.rest;

import java.util.List;

import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Aufgabe 10: Das TRASIMA-Informationssystem als RESTful Service (JAX-RS).
 *
 * <pre>
 * GET    /api/trasima/vehicles        Liste aller Fahrzeuge
 * GET    /api/trasima/vehicles/{id}   ein Fahrzeug                        (404, wenn unbekannt)
 * POST   /api/trasima/vehicles/{id}   Fahrzeug mit Client-Id anlegen      (409, wenn die Id schon existiert)
 * PUT    /api/trasima/vehicles/{id}   Daten eines Fahrzeugs aktualisieren (404, wenn unbekannt)
 * DELETE /api/trasima/vehicles/{id}   Fahrzeug loeschen (Ziel erreicht)   (404, wenn unbekannt)
 * </pre>
 *
 * Der Server ist zustandslos: jede Anfrage enthaelt alles Noetige, es gibt keine Sitzungen.
 * Gespeichert werden nur die Ressourcen selbst (im VehicleStore).
 */
@Path("/trasima/vehicles")
@Produces(MediaType.APPLICATION_JSON)
public class VehicleResource {

    private final VehicleStore store;

    @Inject
    public VehicleResource(VehicleStore store) {
        this.store = store;
    }

    @GET
    public List<VehicleJson> list() {
        return store.getAll().stream().map(VehicleJson::of).toList();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        V2Data data = store.get(id);
        if (data == null) {
            return notFound(id);
        }
        return Response.ok(VehicleJson.of(data)).build();
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("id") String id, VehicleJson body, @Context UriInfo uriInfo) {
        if (body == null) {
            return error(Response.Status.BAD_REQUEST, "Fahrzeugdaten fehlen");
        }
        V2Data data = body.toV2Data(id, System.currentTimeMillis());
        // atomar: nur anlegen, wenn die Id noch frei ist
        if (!store.putIfAbsent(data)) {
            return error(Response.Status.CONFLICT, "Fahrzeug " + id + " existiert bereits");
        }
        return Response.created(uriInfo.getAbsolutePath()).entity(VehicleJson.of(data)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") String id, VehicleJson body) {
        if (body == null) {
            return error(Response.Status.BAD_REQUEST, "Fahrzeugdaten fehlen");
        }
        V2Data data = body.toV2Data(id, System.currentTimeMillis());
        if (!store.replace(data)) {
            return notFound(id);
        }
        return Response.ok(VehicleJson.of(data)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        if (!store.remove(id)) {
            return notFound(id);
        }
        return Response.noContent().build();
    }

    private static Response notFound(String id) {
        return error(Response.Status.NOT_FOUND, "Fahrzeug " + id + " ist nicht bekannt");
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).entity(new ErrorMessage(message)).build();
    }

    /** Fehlerbeschreibung im Body einer Fehlerantwort. */
    public record ErrorMessage(String error) {
    }
}
