package dhbw.trasima.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class V2Test {

    @Test
    void carReachesItsTargetAndPublishesPositions() throws Exception {
        double[] start = Mannheim.place(1);
        double[] corner = { 49.4800, 8.4800 };
        double[] target = Mannheim.place(2);
        LocalInfoSystem info = new LocalInfoSystem();

        V2 car = new V2("T-1", start[0], start[1], 50, List.of(corner, target));
        car.setPublisher(info);
        car.setTrafficInfo(info);
        car.setTiming(1, 5);
        car.start();
        car.join(10_000);

        assertFalse(car.isAlive(), "das V2 sollte angekommen sein");
        V2Data last = info.getAll().get(0);
        assertEquals("T-1", last.id());
        assertEquals(0.0, last.speed());
        assertEquals(0.0, Geo.distanceKm(last.latitude(), last.longitude(), target[0], target[1]), 0.001);
    }

    @Test
    void carWithLargerIdSlowsDownWhenAnotherCarIsClose() throws Exception {
        double[] start = Mannheim.place(1);
        double[] target = Mannheim.place(0);
        VehicleStore store = new VehicleStore();
        LocalInfoSystem info = new LocalInfoSystem(store);
        // ein fahrendes Fahrzeug direkt am Startpunkt, das "immer da" ist
        ITrafficInfo crowded = new ITrafficInfo() {
            @Override
            public List<V2Data> getAll() {
                return info.getAll();
            }

            @Override
            public List<V2Data> getNearby(double lat, double lon, double radiusKm) {
                return List.of(new V2Data("A-0", lat, lon, 30, 0, System.currentTimeMillis()));
            }
        };

        V2 car = new V2("B-1", start[0], start[1], 60, List.of(target));
        car.setPublisher(info);
        car.setTrafficInfo(crowded);
        car.setTiming(1, 5);
        car.start();
        Thread.sleep(200);
        V2Data data = store.get("B-1");
        car.stopDriving();
        car.join(2_000);

        assertTrue(data != null && data.speed() < 60, "das V2 sollte abgebremst haben");
    }

    @Test
    void storeFindsOnlyNearbyVehicles() {
        VehicleStore store = new VehicleStore();
        store.put(new V2Data("near", 49.4880, 8.4662, 50, 0, 1));
        store.put(new V2Data("far", 49.5500, 8.5400, 50, 0, 1));

        List<V2Data> nearby = store.getNearby(Mannheim.CENTER_LAT, Mannheim.CENTER_LON, 0.5);

        assertEquals(1, nearby.size());
        assertEquals("near", nearby.get(0).id());
    }

    @Test
    void osrmResponseIsParsedIntoLatLonWaypoints() {
        String json = "{\"code\":\"Ok\",\"routes\":[{\"geometry\":{\"coordinates\":[[8.4699,49.4796],"
                + "[8.471,49.48],[8.4756,49.484]],\"type\":\"LineString\"}}]}";

        List<double[]> waypoints = OsrmRouteService.parse(json);

        assertEquals(3, waypoints.size());
        assertEquals(49.4796, waypoints.get(0)[0]);
        assertEquals(8.4756, waypoints.get(2)[1]);
    }
}
