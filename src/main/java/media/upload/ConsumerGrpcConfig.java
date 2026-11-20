package media.upload;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.Executor;

public class ConsumerGrpcConfig {
    public Server createServer(int port, int activeLimit, int waitingLimit,
                               Executor uploadWorkers, String ffmpegExecutable,
                               boolean compressionEnabled) throws IOException {
        UploadService uploadService = new UploadService(
                activeLimit, waitingLimit, uploadWorkers, ffmpegExecutable, compressionEnabled);
        return ServerBuilder.forPort(port)
                .executor(uploadWorkers)
                .addService(new UploadGrpcService(uploadService))
                .build();
    }
}
