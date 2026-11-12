package dhbw.trasima.grpc;

import dhbw.trasima.grpc.api.Ack;
import dhbw.trasima.grpc.api.Empty;
import dhbw.trasima.grpc.api.NearbyRequest;
import dhbw.trasima.grpc.api.TrasimaServiceGrpc;
import dhbw.trasima.grpc.api.VehicleData;
import dhbw.trasima.grpc.api.VehicleId;
import dhbw.trasima.grpc.api.VehicleList;
import dhbw.trasima.grpc.api.VehicleReply;
import dhbw.trasima.v2.V2Data;
import dhbw.trasima.v2.VehicleStore;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * Implementierung des gRPC-Dienstes auf Basis des gemeinsamen VehicleStore.
 */
public class TrasimaServiceImpl extends TrasimaServiceGrpc.TrasimaServiceImplBase {

    private final VehicleStore store;

    public TrasimaServiceImpl(VehicleStore store) {
        this.store = store;
    }

    @Override
    public void publishPosition(VehicleData request, StreamObserver<Ack> responseObserver) {
        if (request.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("id fehlt").asRuntimeException());
            return;
        }
        V2Data data = Mapper.toModel(request);
        if (data.timestamp() == 0) {
            data = new V2Data(data.id(), data.latitude(), data.longitude(), data.speed(), data.direction(),
                    System.currentTimeMillis());
        }
        store.put(data);
        responseObserver.onNext(Ack.newBuilder().setOk(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getVehicle(VehicleId request, StreamObserver<VehicleReply> responseObserver) {
        V2Data data = store.get(request.getId());
        VehicleReply.Builder reply = VehicleReply.newBuilder().setFound(data != null);
        if (data != null) {
            reply.setVehicle(Mapper.toMessage(data));
        }
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllVehicles(Empty request, StreamObserver<VehicleList> responseObserver) {
        responseObserver.onNext(Mapper.toMessage(store.getAll()));
        responseObserver.onCompleted();
    }

    @Override
    public void getNearbyVehicles(NearbyRequest request, StreamObserver<VehicleList> responseObserver) {
        responseObserver.onNext(Mapper.toMessage(
                store.getNearby(request.getLatitude(), request.getLongitude(), request.getRadiusKm())));
        responseObserver.onCompleted();
    }

    @Override
    public void removeVehicle(VehicleId request, StreamObserver<Ack> responseObserver) {
        responseObserver.onNext(Ack.newBuilder().setOk(store.remove(request.getId())).build());
        responseObserver.onCompleted();
    }
}
