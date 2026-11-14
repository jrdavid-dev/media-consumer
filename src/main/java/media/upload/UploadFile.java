package media.upload;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

final class UploadFile {
    private Path temporaryFile;
    private OutputStream output;
    private final long expectedBytes;
    private long receivedBytes;

    UploadFile(Path temporaryFile, OutputStream output, long expectedBytes) {
        this.temporaryFile = temporaryFile;
        this.output = output;
        this.expectedBytes = expectedBytes;
    }

    void writeChunk(ByteString chunk) throws IOException {
        if (chunk.isEmpty() || chunk.size() > expectedBytes - receivedBytes) {
            throw new IllegalArgumentException("Invalid chunk size for the remaining upload.");
        }
        chunk.writeTo(output);
        receivedBytes += chunk.size();
    }

    void complete(String originalName) throws IOException {
        if (receivedBytes != expectedBytes) {
            throw new IllegalArgumentException("Received byte count does not match the expected size.");
        }
        output.close();
        output = null;
        String temporaryName = temporaryFile.getFileName().toString();
        String videoName = temporaryName.substring(0, temporaryName.length() - ".part".length()) + ".mp4";
        Path metadata = temporaryFile.resolveSibling(videoName.replace(".mp4", ".name"));
        Files.writeString(metadata, originalName, java.nio.file.StandardOpenOption.CREATE_NEW);
        try {
            Files.move(temporaryFile, temporaryFile.resolveSibling(videoName));
        } catch (IOException error) {
            try {
                Files.deleteIfExists(metadata);
            } catch (IOException cleanupError) {
                error.addSuppressed(cleanupError);
            }
            throw error;
        }
        temporaryFile = null;
    }

    void discard() {
        if (output != null) {
            try {
                output.close();
            } catch (IOException error) {
                System.err.println("Could not close partial upload: " + error.getMessage());
            }
            output = null;
        }

        if (temporaryFile != null) {
            try {
                Files.deleteIfExists(temporaryFile);
                temporaryFile = null;
            } catch (IOException error) {
                System.err.println("Could not delete partial upload: " + error.getMessage());
            }
        }
    }
}
