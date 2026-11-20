package media.upload;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import media.upload.grpc.UploadMetadata;
import media.upload.grpc.UploadRequest;
import media.upload.grpc.UploadResponse;
import media.upload.grpc.UploadStatus;

import java.io.IOException;
import java.util.HexFormat;

final class UploadRequestObserver implements StreamObserver<UploadRequest> {
    private static final int SHA256_BYTES = 32;

    private final UploadService uploadService;
    private final StreamObserver<UploadResponse> responses;

    private UploadFile uploadFile;
    private long expectedBytes;
    private String filename;
    private String expectedDigest;

    private boolean metadataReceived;
    private boolean readySent;
    private boolean finished;

    UploadRequestObserver(UploadService uploadService, StreamObserver<UploadResponse> responses) {
        this.uploadService = uploadService;
        this.responses = responses;
    }

    String filename() {
        return filename;
    }

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
            reject("Expected a supported video filename without path separators and a positive size.");
            return;
        }

        filename = metadata.getFilename();
        expectedBytes = metadata.getExpectedBytes();
        if (metadata.hasSha256()) {
            if (metadata.getSha256().size() != SHA256_BYTES) {
                reject("Expected a 32-byte SHA-256 digest.");
                return;
            }
            expectedDigest = HexFormat.of().formatHex(metadata.getSha256().toByteArray());
            if (uploadService.isDuplicate(expectedDigest)) {
                rejectDuplicate();
                return;
            }
        }

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

        // A different upload may have published the same content while this request waited.
        if (expectedDigest != null && uploadService.isDuplicate(expectedDigest)) {
            rejectDuplicate();
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
    public synchronized void onError(Throwable error) {
        if (finished) {
            return;
        }
        finishUpload();
    }

    @Override
    public synchronized void onCompleted() {
        if (finished) {
            return;
        }

        if (!readySent) {
            reject("Upload ended before the consumer was ready.");
            return;
        }

        try {
            if (!uploadService.completeUpload(uploadFile, filename, expectedDigest)) {
                rejectDuplicate();
                return;
            }
        } catch (IllegalArgumentException error) {
            reject(error.getMessage());
            return;
        } catch (IOException error) {
            System.err.println("Could not save " + filename + ": " + error);
            if (error.getCause() != null) {
                System.err.println(error.getCause());
            }
            fail(Status.INTERNAL, "Could not convert or save the uploaded video. See the consumer log.");
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

    private void rejectDuplicate() {
        String stage = readySent ? "after transfer" : "before transfer";
        System.out.println("Duplicate upload (" + stage + "): " + filename);
        fail(Status.ALREADY_EXISTS, "A video with the same content already exists.");
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
