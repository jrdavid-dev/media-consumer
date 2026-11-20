package media.upload;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

final class UploadStorage {
    private final Path directory = Path.of("uploads");
    private final String ffmpegExecutable;
    private final boolean compressionEnabled;
    private final Set<String> digests = new HashSet<>();

    UploadStorage(String ffmpegExecutable, boolean compressionEnabled) throws IOException {
        this.ffmpegExecutable = ffmpegExecutable;
        this.compressionEnabled = compressionEnabled;
        Files.createDirectories(directory);
        try (var files = Files.list(directory)) {
            for (Path video : files.filter(UploadStorage::isPublishedVideo).toList()) {
                Path sidecar = video.resolveSibling(video.getFileName().toString().replace(".mp4", ".sha256"));
                if (!Files.exists(sidecar)) {
                    backfillDigest(video, sidecar);
                }
                String digest = Files.readString(sidecar).strip();
                if (!digest.matches("[0-9a-f]{64}")) {
                    throw new IOException("Invalid SHA-256 sidecar: " + sidecar);
                }
                digests.add(digest);
            }
        }
    }

    private void backfillDigest(Path video, Path sidecar) throws IOException {
        var digest = UploadFile.newDigest();
        try (var input = Files.newInputStream(video);
             var hashedInput = new DigestInputStream(input, digest)) {
            hashedInput.transferTo(OutputStream.nullOutputStream());
        }
        Path temporary = Files.createTempFile(directory, "digest-", ".part");
        try {
            Files.writeString(temporary, HexFormat.of().formatHex(digest.digest()));
            Files.move(temporary, sidecar, ATOMIC_MOVE);
        } finally {
            UploadFile.deleteTemporary(temporary);
        }
    }

    static boolean isPublishedVideo(Path file) {
        return file.getFileName().toString().matches("video-[0-9]+\\.mp4") && Files.isRegularFile(file);
    }

    UploadFile openUpload(long expectedBytes) throws IOException {
        Path temporaryFile = Files.createTempFile(directory, "video-", ".part");
        try {
            return new UploadFile(temporaryFile, Files.newOutputStream(temporaryFile),
                    expectedBytes, ffmpegExecutable, compressionEnabled);
        } catch (IOException error) {
            UploadFile.deleteTemporary(temporaryFile);
            throw error;
        }
    }

    synchronized boolean containsDigest(String digest) {
        return digests.contains(digest);
    }

    synchronized boolean publish(Path source, Path original, String filename, String digest) throws IOException {
        if (digests.contains(digest)) {
            return false;
        }

        String temporaryName = original.getFileName().toString();
        String id = temporaryName.substring(0, temporaryName.length() - ".part".length());
        Path nameSidecar = null;
        Path digestSidecar = null;
        try {
            nameSidecar = Files.createFile(directory.resolve(id + ".name"));
            Files.writeString(nameSidecar, filename);
            digestSidecar = Files.createFile(directory.resolve(id + ".sha256"));
            Files.writeString(digestSidecar, digest);
            // Publish the MP4 last so gallery requests see a complete video and its metadata.
            Files.move(source, directory.resolve(id + ".mp4"), ATOMIC_MOVE);
            digests.add(digest);
            return true;
        } catch (IOException error) {
            UploadFile.deleteTemporary(digestSidecar);
            UploadFile.deleteTemporary(nameSidecar);
            throw error;
        }
    }
}
