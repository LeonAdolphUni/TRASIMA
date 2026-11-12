package dhbw.trasima.grpc.demo;

import dhbw.trasima.grpc.demo.MyDemoServiceGrpc;
import dhbw.trasima.grpc.demo.MyDemoServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ClientApp {
  public static void main(String[] args) throws Exception {
    // Channel is the abstraction to connect to a service endpoint
    // Let's use plaintext communication because we don't have certs
    final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + (args.length > 0 ? args[0] : "8080"))
        .usePlaintext(true)
        .build();

    // It is up to the client to determine whether to block the call
    // Here we create a blocking stub, but an async stub,
    // or an async stub with Future are always possible.
    MyDemoServiceGrpc.MyDemoServiceBlockingStub stub = MyDemoServiceGrpc.newBlockingStub(channel);
    MyDemoServiceOuterClass.HelloRequest request = MyDemoServiceOuterClass.HelloRequest.newBuilder()
        .setName("Trasima")
        .build();

    // Finally, make the call using the stub
    MyDemoServiceOuterClass.HelloResponse response = stub.greeting(request);

    System.out.println(response);

    // A Channel should be shutdown before stopping the process.
    channel.shutdownNow();
  }
}