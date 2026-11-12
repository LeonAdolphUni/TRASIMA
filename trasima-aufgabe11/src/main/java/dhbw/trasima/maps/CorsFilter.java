package dhbw.trasima.maps;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Erlaubt Aufrufe aus anderen Urspruengen (CORS), z. B. wenn die Karte als Datei
 * (file://.../index.html) oder von einem anderen Port aus geoeffnet wird.
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        response.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        response.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type");
        response.getHeaders().putSingle("Cache-Control", "no-store");
    }
}
