# Creating a gRPC project within the TRASIMA parent project

## setup the maven project and add the structure for the proto files 

You are in the folder of the parent project then enter the following commands

```bash
mvn -B archetype:generate  -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5   -DgroupId=dhbw.trasima.grpc  -DartifactId=trasima-aufgabe06 -Dversion=1.0.0-SNAPSHOT

cd trasima-aufgabe06

mkdir -p src/main/proto
```

## add dependencies and adapt the build process

In order to use the grpc functionality Maven needs 
* the dependencies to the framework 
+ and the integration of the gRPC tools in the build process

Therefore add the following lines into the pom.xml of the child project 
directly after ```<dependencies>```

```xml
 <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-netty-shaded</artifactId>
      <version>1.24.0</version>
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-protobuf</artifactId>
      <version>1.24.0</version>
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-stub</artifactId>
      <version>1.24.0</version>
    </dependency>
    <dependency>
      <groupId>javax.annotation</groupId>
      <artifactId>javax.annotation-api</artifactId>
      <version>1.3.2</version>
    </dependency>
```

Then add the build instructions for gRPC in the section ```<build>```

```xml
  </dependencies>
  <build>
    <extensions>
      <extension>
        <groupId>kr.motd.maven</groupId>
        <artifactId>os-maven-plugin</artifactId>
        <version>1.6.2</version>
      </extension>
    </extensions>
    <plugins>
      <plugin>
        <groupId>org.xolstice.maven.plugins</groupId>
        <artifactId>protobuf-maven-plugin</artifactId>
        <version>0.6.1</version>
        <configuration>
          <protocArtifact>com.google.protobuf:protoc:3.9.0:exe:${os.detected.classifier}</protocArtifact>
          <pluginId>grpc-java</pluginId>
          <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.24.0:exe:${os.detected.classifier}</pluginArtifact>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>compile</goal>
              <goal>compile-custom</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

## edit the MyDemoService.proto file 

If done so, you are ready to define the first gRPC interface. 

1. Create the file src/main/proto/MyDemoService.proto 
2. and enter the following code
```
syntax = "proto3";
package dhbw.trasima.grpc.demo;

// Request payload
message HelloRequest {
    // Each message attribute is strongly typed.
    // You also must assign a "tag" number.
    // Each tag number is unique within the message.
    string name = 1;

    // This defines a strongly typed list of String
    repeated string hobbies = 2;

    // There are many more basics types, like Enum, Map
    // See https://developers.google.com/protocol-buffers/docs/proto3
    // for more information.
}

message HelloResponse {
    string greeting = 1;
}

// Defining a Service, a Service can have multiple RPC operations
service MyDemoService {
    // Define a RPC operation
    rpc greeting(HelloRequest) returns (HelloResponse);
}
```
3. and save the file

## compile the proto-stubs for the service

Now it is time to create the stubs for the fRPC services with the help of Maven 

```bash 
mvn -DskipTests package
```

You will find the generated java in the folder ```target/generated_sources```

Based on the stubs you can implement the service and the client 

## Implement the service 

* create the file ```MyDemoServiceImpl.java" and add the following 
content

```java
package dhbw.trasima.grpc.demo; 

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
```

## Provide the service with a server  

* create the Klasse ```ServerApp.java```
* add the following content

```java
package dhbw.trasima.grpc.demo;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ServerApp
{
    public static void main( String[] args ) throws Exception
    {
      // Create a new server to listen on port 8080
      Server server = ServerBuilder.forPort(8080)
        .addService(new MyDemoServiceImpl())
        .build();

      // Start the server
      server.start();

      // Server threads are running in the background.
      System.out.println("Server started");
      // Don't exit the main thread. Wait until server is terminated.
      server.awaitTermination();
    }
}
```

* you can start the server with the IDE or with the help of Maven like 

```bash 
mvn -DskipTests package exec:java -"Dexec.mainClass=dhbw.trasima.grpc.ServerApp"
```

## Create an examplary client application  

* create the Klasse ```ClientApp.java```
* add the following content

```java
package dhbw.trasima.grpc.demo;

import io.grpc.*;

public class ClientApp {
  public static void main(String[] args) throws Exception {
    // Channel is the abstraction to connect to a service endpoint
    // Let's use plaintext communication because we don't have certs
    final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8080")
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
```
you can start this client with the IDE or with the help of Maven like 

```bash 
mvn -DskipTests package exec:java -"Dexec.mainClass=dhbw.trasima.grpc.ClientApp"
```

# Hints

Sometimes the classpath of the VisualCode IDE does not update the classpaths correctly. I found out, that the following procedure can help: 

1. enter ```Strg-Shift-p"
2. Enter "Java: Clean Java Language Server Workspace"

