package media.upload;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;

public class UploadService {
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "mkv", "webm", "m4v", "avi", "mpg", "mpeg", "ts", "mts", "m2ts", "flv", "wmv", "ogv", "3gp");
    private final UploadQueue queue;
    private final UploadStorage storage;
    private final Executor uploadWorkers;

    public UploadService(int activeLimit, int waitingLimit,
                         Executor uploadWorkers, String ffmpegExecutable, boolean compressionEnabled) throws IOException {
        this.uploadWorkers = uploadWorkers;
        queue = new UploadQueue(activeLimit, waitingLimit);
        storage = new UploadStorage(ffmpegExecutable, compressionEnabled);
    }

    UploadQueue.Admission admit(UploadRequestObserver upload) {
        return queue.admit(upload);
    }

    void release(UploadRequestObserver upload) {
        UploadRequestObserver next = queue.release(upload);
        if (next != null) {
            // Start outside the queue lock without taking two observer locks on one thread.
            uploadWorkers.execute(next::startUpload);
        }
    }

    public boolean validate(String filename, long size) {
        if (filename == null || filename.contains("/") || filename.indexOf('\\') >= 0 || size <= 0) {
            return false;
        }

        int dot = filename.lastIndexOf('.');
        return dot > 0 && !filename.substring(0, dot).isBlank()
                && VIDEO_EXTENSIONS.contains(filename.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    UploadFile openUpload(long expectedBytes) throws IOException {
        return storage.openUpload(expectedBytes);
    }

    boolean isDuplicate(String digest) {
        return storage.containsDigest(digest);
    }

    boolean completeUpload(UploadFile file, String filename, String expectedDigest) throws IOException {
        return file.complete(filename, expectedDigest, storage);
    }
}
