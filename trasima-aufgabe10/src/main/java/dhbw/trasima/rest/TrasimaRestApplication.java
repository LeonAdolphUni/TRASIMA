package dhbw.trasima.rest;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import dhbw.trasima.v2.VehicleStore;

/**
 * Definiert die REST-Anwendung: welche Dienste (POJOs) bereitgestellt werden.
 * Der gemeinsame VehicleStore wird den Ressourcen per Dependency Injection uebergeben.
 */
public class TrasimaRestApplication extends ResourceConfig {

    public TrasimaRestApplication(VehicleStore store) {
        register(VehicleResource.class);
        register(JacksonFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(store).to(VehicleStore.class);
            }
        });
        // die automatische WADL-Beschreibung wird nicht gebraucht (und braeuchte JAXB)
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
    }
}
