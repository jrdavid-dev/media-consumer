package media.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private final Path uploads = Path.of("uploads");

    public record Video(String id, String filename) {}

    @GetMapping
    public List<Video> list() throws IOException {
        if (!Files.isDirectory(uploads)) {
            return List.of();
        }

        try (var files = Files.list(uploads)) {
            var videos = new java.util.ArrayList<Video>();
            for (Path file : files.sorted().toList()) {
                String name = file.getFileName().toString();
                if (!UploadStorage.isPublishedVideo(file)) {
                    continue;
                }
                String id = name.substring(0, name.length() - 4);
                Path metadata = uploads.resolve(id + ".name");
                videos.add(new Video(id, Files.isRegularFile(metadata) ? Files.readString(metadata) : name));
            }
            return videos;
        }
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> content(@PathVariable("id") String id) {
        if (!id.matches("video-[0-9]+")) {
            return ResponseEntity.notFound().build();
        }
        Path file = uploads.resolve(id + ".mp4");
        if (!Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(new FileSystemResource(file));
    }
}
