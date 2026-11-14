package media.upload;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ConsumerGrpcConfig {
    public Server createServer(int port, int activeLimit, int waitingLimit) {
        UploadService uploadService = new UploadService(activeLimit, waitingLimit);
        return ServerBuilder.forPort(port)
                .addService(new UploadGrpcService(uploadService))
                .build();
    }
}
