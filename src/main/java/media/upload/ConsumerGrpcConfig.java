package media.upload;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ConsumerGrpcConfig {
    public Server createServer(int port) {
        UploadService uploadService = new UploadService();
        return ServerBuilder.forPort(port)
                .addService(new UploadGrpcService(uploadService))
                .build();
    }
}
