package media.upload;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

final class UploadFile {
    private static final int TRANSCODE_TIMEOUT_SECONDS = 120;

    private final Path temporaryFile;
    private final long expectedBytes;
    private final String ffmpegExecutable;
    private final boolean compressionEnabled;
    private final MessageDigest digest = newDigest();
    private OutputStream output;
    private long receivedBytes;

    UploadFile(Path temporaryFile, OutputStream output, long expectedBytes, String ffmpegExecutable,
               boolean compressionEnabled) {
        this.temporaryFile = temporaryFile;
        this.output = new DigestOutputStream(output, digest);
        this.expectedBytes = expectedBytes;
        this.ffmpegExecutable = ffmpegExecutable;
        this.compressionEnabled = compressionEnabled;
    }

    static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable.", error);
        }
    }

    void writeChunk(ByteString chunk) throws IOException {
        if (chunk.isEmpty() || chunk.size() > expectedBytes - receivedBytes) {
            throw new IllegalArgumentException("Invalid chunk size for the remaining upload.");
        }
        chunk.writeTo(output);
        receivedBytes += chunk.size();
    }

    boolean complete(String originalName, String expectedDigest, UploadStorage storage) throws IOException {
        String originalDigest = verifyReceivedFile(expectedDigest);
        boolean needsConversion = !originalName.toLowerCase(Locale.ROOT).endsWith(".mp4");
        if (!needsConversion && !compressionEnabled) {
            return storage.publish(temporaryFile, temporaryFile, originalName, originalDigest);
        }

        Path converted;
        try {
            converted = prepareTranscodedVideo(originalName, needsConversion);
        } catch (IOException error) {
            if (needsConversion) {
                throw new IOException("MP4 conversion failed for " + originalName, error);
            }
            System.err.printf("Compression skipped for %s: %s Keeping original.%n",
                    originalName, error.getMessage());
            return storage.publish(temporaryFile, temporaryFile, originalName, originalDigest);
        }

        try {
            return storage.publish(converted, temporaryFile, originalName, originalDigest);
        } finally {
            deleteTemporary(converted);
        }
    }

    private String verifyReceivedFile(String expectedDigest) throws IOException {
        if (receivedBytes != expectedBytes) {
            throw new IllegalArgumentException("Received byte count does not match the expected size.");
        }
        output.close();
        output = null;
        String originalDigest = HexFormat.of().formatHex(digest.digest());
        if (expectedDigest != null && !expectedDigest.equals(originalDigest)) {
            throw new IllegalArgumentException("Received content does not match the supplied SHA-256 digest.");
        }
        return originalDigest;
    }

    private Path prepareTranscodedVideo(String originalName, boolean needsConversion) throws IOException {
        Path converted = Files.createTempFile(temporaryFile.getParent(), "transcoded-", ".part");
        try {
            transcode(converted);
            long convertedBytes = Files.size(converted);
            if (convertedBytes == 0) {
                throw new IOException("FFmpeg produced an empty video.");
            }
            if (!needsConversion && convertedBytes >= expectedBytes) {
                throw new IOException("FFmpeg did not produce a smaller video.");
            }
            System.out.printf("%s %s to MP4 (%d to %d bytes).%n",
                    needsConversion ? "Converted" : "Compressed", originalName, expectedBytes, convertedBytes);
            return converted;
        } catch (IOException error) {
            deleteTemporary(converted);
            throw error;
        }
    }

    private void transcode(Path destination) throws IOException {
        Process process = new ProcessBuilder(ffmpegExecutable, "-nostdin", "-y", "-v", "error", "-xerror", "-abort_on", "empty_output_stream",
                "-protocol_whitelist", "file",
                "-format_whitelist", "mov,matroska,webm,m4v,avi,mpeg,mpegts,flv,asf,ogg",
                "-i", temporaryFile.toString(), "-map", "0:V:0", "-map", "0:a?",
                "-vf", "pad=ceil(iw/2)*2:ceil(ih/2)*2",
                "-c:v", "libx264", "-preset", "fast", "-crf", compressionEnabled ? "28" : "18", "-pix_fmt", "yuv420p",
                "-c:a", "aac", "-movflags", "+faststart", "-f", "mp4", destination.toString())
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();
        try {
            if (!process.waitFor(TRANSCODE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IOException("FFmpeg timed out.");
            }
            if (process.exitValue() != 0) {
                throw new IOException("FFmpeg exited with code " + process.exitValue() + ".");
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IOException("FFmpeg was interrupted.", error);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
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
        deleteTemporary(temporaryFile);
    }

    static void deleteTemporary(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException error) {
            System.err.println("Could not clean up " + file + ": " + error.getMessage());
        }
    }
}
