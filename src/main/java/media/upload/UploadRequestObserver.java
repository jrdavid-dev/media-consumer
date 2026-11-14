package media.upload;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import media.upload.grpc.UploadMetadata;
import media.upload.grpc.UploadRequest;
import media.upload.grpc.UploadResponse;
import media.upload.grpc.UploadStatus;

import java.io.IOException;

final class UploadRequestObserver implements StreamObserver<UploadRequest> {
    private final UploadService uploadService;
    private final StreamObserver<UploadResponse> responses;

    UploadRequestObserver(UploadService uploadService, StreamObserver<UploadResponse> responses) {
        this.uploadService = uploadService;
        this.responses = responses;
    }

    private UploadFile uploadFile;
    private boolean readySent;
    private boolean finished;

    @Override
    public void onNext(UploadRequest request) {
        if (finished) {
            return;
        }

        if (!readySent) {
            handleMetadata(request);
            return;
        }

        handleChunk(request);
    }

    private void handleMetadata(UploadRequest request) {
        if (!request.hasMetadata()) {
            reject("Send exactly one metadata message for the handshake.");
            return;
        }

        UploadMetadata metadata = request.getMetadata();
        if (!uploadService.validate(metadata.getFilename(), metadata.getExpectedBytes())) {
            reject("Expected an MP4 filename without path separators and a positive size.");
            return;
        }

        try {
            uploadFile = uploadService.openUpload(metadata.getExpectedBytes());
        } catch (IOException error) {
            fail(Status.INTERNAL, "Could not open the upload file.");
            return;
        }

        readySent = true;
        UploadResponse response = UploadResponse.newBuilder()
                .setStatus(UploadStatus.READY)
                .build();
        responses.onNext(response);
    }

    private void handleChunk(UploadRequest request) {
        if (!request.hasChunk()) {
            reject("Expected a video chunk.");
            return;
        }

        try {
            uploadFile.writeChunk(request.getChunk());
        } catch (IllegalArgumentException error) {
            reject(error.getMessage());
        } catch (IOException error) {
            fail(Status.INTERNAL, "Could not write the video chunk.");
        }
    }

    @Override
    public void onError(Throwable error) {
        finished = true;
        discardPartialFile();
    }

    @Override
    public void onCompleted() {
        if (finished) {
            return;
        }

        if (!readySent) {
            reject("Upload metadata is required.");
            return;
        }

        try {
            uploadFile.complete();
        } catch (IllegalArgumentException error) {
            reject(error.getMessage());
            return;
        } catch (IOException error) {
            fail(Status.INTERNAL, "Could not save the completed video.");
            return;
        }

        finished = true;
        UploadResponse response = UploadResponse.newBuilder()
                .setStatus(UploadStatus.COMPLETED)
                .build();
        responses.onNext(response);
        responses.onCompleted();
    }

    private void reject(String message) {
        fail(Status.INVALID_ARGUMENT, message);
    }

    private void fail(Status status, String message) {
        finished = true;
        discardPartialFile();
        responses.onError(status
                .withDescription(message)
                .asRuntimeException());
    }

    private void discardPartialFile() {
        if (uploadFile != null) {
            uploadFile.discard();
        }
    }
}
