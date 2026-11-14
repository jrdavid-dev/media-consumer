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
    private long expectedBytes;
    private String filename;

    String filename() {
        return filename;
    }
    private boolean metadataReceived;
    private boolean readySent;
    private boolean finished;

    @Override
    public synchronized void onNext(UploadRequest request) {
        if (finished) {
            return;
        }

        if (!metadataReceived) {
            handleMetadata(request);
            return;
        }

        if (!readySent) {
            reject("Wait for READY before sending video chunks.");
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

        filename = metadata.getFilename();
        expectedBytes = metadata.getExpectedBytes();
        metadataReceived = true;
        switch (uploadService.admit(this)) {
            case START_NOW -> startUpload();
            case QUEUED -> sendStatus(UploadStatus.QUEUED);
            case FULL -> fail(Status.RESOURCE_EXHAUSTED, "Upload queue is full.");
        }
    }

    synchronized void startUpload() {
        if (finished || readySent) {
            return;
        }

        try {
            uploadFile = uploadService.openUpload(expectedBytes);
        } catch (IOException error) {
            fail(Status.INTERNAL, "Could not open the upload file.");
            return;
        }

        readySent = true;
        sendStatus(UploadStatus.READY);
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
        if (finished) {
            return;
        }
        finishUpload();
    }

    @Override
    public void onCompleted() {
        if (finished) {
            return;
        }

        if (!readySent) {
            reject("Upload ended before the consumer was ready.");
            return;
        }

        try {
            uploadFile.complete(filename);
        } catch (IllegalArgumentException error) {
            reject(error.getMessage());
            return;
        } catch (IOException error) {
            fail(Status.INTERNAL, "Could not save the completed video.");
            return;
        }

        try {
            System.out.println("Video received: " + filename + " (" + expectedBytes + " bytes)");
            sendStatus(UploadStatus.COMPLETED);
            responses.onCompleted();
        } finally {
            finishUpload();
        }
    }

    private void reject(String message) {
        fail(Status.INVALID_ARGUMENT, message);
    }

    private void fail(Status status, String message) {
        finishUpload();
        responses.onError(status
                .withDescription(message)
                .asRuntimeException());
    }

    private void finishUpload() {
        finished = true;
        discardPartialFile();
        uploadService.release(this);
    }

    private void sendStatus(UploadStatus status) {
        responses.onNext(UploadResponse.newBuilder().setStatus(status).build());
    }

    private void discardPartialFile() {
        if (uploadFile != null) {
            uploadFile.discard();
        }
    }
}
