package media.upload;

import io.grpc.Server;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 9090;
        Server server = new ConsumerGrpcConfig().createServer(port);
        server.start();
        System.out.println("Consumer gRPC server listening on port " + port);
        server.awaitTermination();
    }
}
