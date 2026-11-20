package media.upload;

import io.grpc.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.Executors;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner input = new Scanner(System.in);
        Integer activeLimit = readLimit(input, "Maximum active uploads (c): ", 1);
        if (activeLimit == null) {
            return;
        }
        Integer waitingLimit = readLimit(input, "Maximum waiting uploads (q): ", 0);
        if (waitingLimit == null) {
            return;
        }

        boolean compressionEnabled = readCompression(input);
        String ffmpegExecutable = System.getenv().getOrDefault("UPLOAD_FFMPEG", "");
        if (ffmpegExecutable.isBlank()) {
            ffmpegExecutable = "ffmpeg";
        }
        int port = 9090;
        try (var uploadWorkers = Executors.newFixedThreadPool(activeLimit)) {
            Server server = new ConsumerGrpcConfig()
                    .createServer(port, activeLimit, waitingLimit, uploadWorkers, ffmpegExecutable, compressionEnabled);
            server.start();
            System.out.println("Consumer gRPC server listening on port " + port);
            try (var web = SpringApplication.run(Main.class, "--server.port=8080")) {
                server.awaitTermination();
            } finally {
                server.shutdownNow();
                server.awaitTermination();
            }
        }
    }

    private static boolean readCompression(Scanner input) {
        while (true) {
            System.out.print("Enable video compression? [y/N]: ");
            if (!input.hasNextLine()) {
                return false;
            }

            switch (input.nextLine().trim().toLowerCase(Locale.ROOT)) {
                case "y", "yes" -> {
                    return true;
                }
                case "", "n", "no" -> {
                    return false;
                }
                default -> System.err.println("Enter yes or no.");
            }
        }
    }

    private static Integer readLimit(Scanner input, String prompt, int minimum) {
        while (true) {
            System.out.print(prompt);
            if (!input.hasNextLine()) {
                return null;
            }

            try {
                int limit = Integer.parseInt(input.nextLine().trim());
                if (limit >= minimum) {
                    return limit;
                }
            } catch (NumberFormatException error) {
                // Prompt again for a valid whole number.
            }

            System.err.println("Enter a whole number of at least " + minimum + ".");
        }
    }
}
