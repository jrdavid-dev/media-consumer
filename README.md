# Media Consumer

Media Consumer receives videos from the [Media Producer](https://github.com/jrdavid-dev/media-producer) over gRPC. It limits concurrent uploads, queues excess requests up to a configured limit, and uses SHA-256 hashes to reject duplicates before the producer sends video bytes. The bundled React gallery lists completed uploads and plays them in the browser.

## Build

Install JDK 21 or newer, Maven, and Node.js 22 or newer with npm. From the repository root, run:

```sh
(cd frontend && npm ci)
mvn clean package -Pbundle-gallery
```

The Maven profile builds the Vite frontend and bundles it into `target/consumer-0.1.0.jar`.

## Run

```sh
java -jar target/consumer-0.1.0.jar
```

1. Enter the maximum active uploads (`c`, at least 1) and the maximum waiting uploads (`q`, at least 0). For example, `c=1` and `q=1` allow one active upload and one waiting upload. With `q=0`, new uploads are rejected while all active slots are occupied.
2. Choose whether to enable video compression. Press Enter to use the default of no compression.
3. Start Media Producer and connect it to the consumer on TCP port `9090`. Use `localhost` on the same machine or the consumer's IP on another machine.
4. Open the gallery on port `8080`. Hover over a clip for a preview or click it for full playback. [Open the local gallery](http://localhost:8080)

Videos and their metadata are saved under `uploads/` relative to the working directory. Keep the sidecar files with the videos so the consumer can retain original names and detect duplicates across restarts.

## Conversion and compression

Install FFmpeg with the `libx264` encoder to process uploads in formats other than MP4 or to enable compression. The consumer looks for `ffmpeg` on `PATH`. Set `UPLOAD_FFMPEG` to the executable's full path if needed.

The consumer converts supported non-MP4 uploads to H.264 MP4 for playback. If required conversion fails, the upload fails. With compression enabled, the consumer tries to shrink incoming MP4s and uses a smaller encoding target for other formats. For incoming MP4s, it keeps the original if compression fails, times out, or produces an equal-size or larger file. Duplicate detection uses the original uploaded bytes.
