package dhbw.trasima.grpc.demo; 

import dhbw.trasima.grpc.demo.MyDemoServiceGrpc;
import dhbw.trasima.grpc.demo.MyDemoServiceOuterClass;
import io.grpc.stub.StreamObserver;

public class MyDemoServiceImpl extends MyDemoServiceGrpc.MyDemoServiceImplBase {
    @Override
    public void greeting(MyDemoServiceOuterClass.HelloRequest request,
        StreamObserver<MyDemoServiceOuterClass.HelloResponse> responseObserver) {
        // HelloRequest has toString auto-generated.
        System.out.println(request);

    // You must use a builder to construct a new Protobuffer object
    MyDemoServiceOuterClass.HelloResponse response = MyDemoServiceOuterClass.HelloResponse.newBuilder()
        .setGreeting("Hello there, " + request.getName())
        .build();

    // Use responseObserver to send a single response back
    responseObserver.onNext(response);

    // When you are done, you must call onCompleted.
    responseObserver.onCompleted();
    }
}
