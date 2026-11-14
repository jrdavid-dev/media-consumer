package media.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class UploadService {
    private final UploadQueue queue;

    public UploadService(int activeLimit, int waitingLimit) {
        queue = new UploadQueue(activeLimit, waitingLimit);
    }

    UploadQueue.Admission admit(UploadRequestObserver upload) {
        return queue.admit(upload);
    }

    void release(UploadRequestObserver upload) {
        UploadRequestObserver next = queue.release(upload);
        if (next != null) {
            // Start outside the queue lock without taking two observer locks on one thread.
            Thread.startVirtualThread(next::startUpload);
        }
    }

    public boolean validate(String filename, long size) {
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
            return false;
        }

        String nameWithoutExtension = filename.substring(0, filename.length() - 4);
        if (nameWithoutExtension.isBlank()) {
            return false;
        }

        if (filename.contains("/") || filename.indexOf('\\') >= 0) {
            return false;
        }

        if (size <= 0) {
            return false;
        }

        return true;
    }

    UploadFile openUpload(long expectedBytes) throws IOException {
        Path uploadDirectory = Files.createDirectories(Path.of("uploads"));
        Path temporaryFile = Files.createTempFile(uploadDirectory, "video-", ".part");
        try {
            return new UploadFile(temporaryFile, Files.newOutputStream(temporaryFile), expectedBytes);
        } catch (IOException error) {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupError) {
                error.addSuppressed(cleanupError);
            }
            throw error;
        }
    }
}
