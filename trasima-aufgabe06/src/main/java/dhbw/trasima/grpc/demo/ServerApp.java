package dhbw.trasima.grpc.demo;

import dhbw.trasima.grpc.demo.MyDemoServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ServerApp
{
    public static void main( String[] args ) throws Exception
    {
      // Create a new server to listen on port 8080 (or the port given as first argument)
      int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
      Server server = ServerBuilder.forPort(port)
        .addService(new MyDemoServiceImpl())
        .build();

      // Start the server
      server.start();

      // Server threads are running in the background.
      System.out.println("Server started on port " + port);
      // Don't exit the main thread. Wait until server is terminated.
      server.awaitTermination();
    }
}

