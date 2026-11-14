package media.upload;

import io.grpc.stub.StreamObserver;
import media.upload.grpc.UploadRequest;
import media.upload.grpc.UploadResponse;
import media.upload.grpc.UploadServiceGrpc;

public class UploadGrpcService extends UploadServiceGrpc.UploadServiceImplBase {
    private final UploadService uploadService;

    public UploadGrpcService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @Override
    public StreamObserver<UploadRequest> upload(
            StreamObserver<UploadResponse> responses) {

        return new UploadRequestObserver(uploadService, responses);
    }
}
