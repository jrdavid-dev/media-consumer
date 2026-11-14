package media.upload;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

final class UploadQueue {
    enum Admission {
        START_NOW,
        QUEUED,
        FULL
    }

    private final int activeLimit;
    private final int waitingLimit;
    private final Set<UploadRequestObserver> activeUploads = new HashSet<>();
    private final Deque<UploadRequestObserver> waitingUploads = new ArrayDeque<>();

    UploadQueue(int activeLimit, int waitingLimit) {
        if (activeLimit < 1 || waitingLimit < 0) {
            throw new IllegalArgumentException("Expected at least one active slot and zero or more waiting slots.");
        }
        this.activeLimit = activeLimit;
        this.waitingLimit = waitingLimit;
    }

    synchronized Admission admit(UploadRequestObserver upload) {
        if (activeUploads.contains(upload)) {
            return Admission.START_NOW;
        }
        if (waitingUploads.contains(upload)) {
            return Admission.QUEUED;
        }

        if (activeUploads.size() < activeLimit) {
            activeUploads.add(upload);
            logState("Admitted", upload);
            return Admission.START_NOW;
        }

        if (waitingUploads.size() < waitingLimit) {
            waitingUploads.addLast(upload);
            logState("Queued", upload);
            return Admission.QUEUED;
        }

        logState("Rejected (queue full)", upload);
        return Admission.FULL;
    }

    synchronized UploadRequestObserver release(UploadRequestObserver upload) {
        if (!activeUploads.remove(upload)) {
            if (waitingUploads.remove(upload)) {
                logState("Removed from waiting queue", upload);
            }
            return null;
        }

        UploadRequestObserver nextUpload = waitingUploads.pollFirst();
        if (nextUpload != null) {
            activeUploads.add(nextUpload);
        }

        logState("Released", upload);
        if (nextUpload != null) {
            logState("Promoted", nextUpload);
        }
        return nextUpload;
    }

    private void logState(String event, UploadRequestObserver upload) {
        System.out.printf("%n[Consumer] %s%n  Video:   %s%n  Active:  %d/%d %s%n  Waiting: %d/%d %s%n",
                event, upload.filename(),
                activeUploads.size(), activeLimit,
                activeUploads.stream().map(UploadRequestObserver::filename).sorted().toList(),
                waitingUploads.size(), waitingLimit,
                waitingUploads.stream().map(UploadRequestObserver::filename).toList());
    }
}
