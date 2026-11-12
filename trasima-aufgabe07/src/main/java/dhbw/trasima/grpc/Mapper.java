package dhbw.trasima.grpc;

import java.util.ArrayList;
import java.util.List;

import dhbw.trasima.grpc.api.VehicleData;
import dhbw.trasima.grpc.api.VehicleList;
import dhbw.trasima.v2.V2Data;

/**
 * Wandelt zwischen dem Modell (V2Data) und den generierten Protobuf-Nachrichten um.
 */
final class Mapper {

    private Mapper() {
    }

    static VehicleData toMessage(V2Data data) {
        return VehicleData.newBuilder()
                .setId(data.id())
                .setLatitude(data.latitude())
                .setLongitude(data.longitude())
                .setSpeed(data.speed())
                .setDirection(data.direction())
                .setTimestamp(data.timestamp())
                .build();
    }

    static V2Data toModel(VehicleData message) {
        return new V2Data(message.getId(), message.getLatitude(), message.getLongitude(), message.getSpeed(),
                message.getDirection(), message.getTimestamp());
    }

    static VehicleList toMessage(List<V2Data> vehicles) {
        VehicleList.Builder list = VehicleList.newBuilder();
        for (V2Data data : vehicles) {
            list.addVehicles(toMessage(data));
        }
        return list.build();
    }

    static List<V2Data> toModel(VehicleList list) {
        List<V2Data> vehicles = new ArrayList<>(list.getVehiclesCount());
        for (VehicleData message : list.getVehiclesList()) {
            vehicles.add(toModel(message));
        }
        return vehicles;
    }
}
