package dhbw.trasima.grpc;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dhbw.trasima.grpc.api.Empty;
import dhbw.trasima.grpc.api.NearbyRequest;
import dhbw.trasima.grpc.api.TrasimaServiceGrpc;
import dhbw.trasima.grpc.api.VehicleId;
import dhbw.trasima.grpc.api.VehicleReply;
import dhbw.trasima.v2.IPositionPublisher;
import dhbw.trasima.v2.ITrafficInfo;
import dhbw.trasima.v2.V2Data;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Client fuer den TRASIMA-gRPC-Server. Implementiert dieselben Schnittstellen wie der Socket-Client,
 * deshalb muss am V2 nichts geaendert werden. Der Stub ist thread-sicher und wird von allen V2s geteilt.
 */
public class GrpcTrasimaClient implements IPositionPublisher, ITrafficInfo, Closeable {

    private static final long DEADLINE_SECONDS = 5;

    private final ManagedChannel channel;
    private final TrasimaServiceGrpc.TrasimaServiceBlockingStub stub;

    public GrpcTrasimaClient(String host, int port) {
        // Klartext-Verbindung, weil wir keine Zertifikate haben
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = TrasimaServiceGrpc.newBlockingStub(channel);
    }

    private TrasimaServiceGrpc.TrasimaServiceBlockingStub stub() {
        return stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void publish(V2Data data) {
        stub().publishPosition(Mapper.toMessage(data));
    }

    /** @return die Daten des Fahrzeugs oder null, wenn der Server es nicht kennt */
    public V2Data get(String id) {
        VehicleReply reply = stub().getVehicle(VehicleId.newBuilder().setId(id).build());
        return reply.getFound() ? Mapper.toModel(reply.getVehicle()) : null;
    }

    /** @return true, wenn das Fahrzeug vorhanden war */
    public boolean remove(String id) {
        return stub().removeVehicle(VehicleId.newBuilder().setId(id).build()).getOk();
    }

    @Override
    public List<V2Data> getAll() {
        return Mapper.toModel(stub().getAllVehicles(Empty.getDefaultInstance()));
    }

    @Override
    public List<V2Data> getNearby(double latitude, double longitude, double radiusKm) {
        return Mapper.toModel(stub().getNearbyVehicles(NearbyRequest.newBuilder()
                .setLatitude(latitude).setLongitude(longitude).setRadiusKm(radiusKm).build()));
    }

    @Override
    public void close() {
        channel.shutdownNow();
    }
}
