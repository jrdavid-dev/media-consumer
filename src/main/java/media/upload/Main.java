package media.upload;

import io.grpc.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.IOException;
import java.util.Scanner;

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

        int port = 9090;
        Server server = new ConsumerGrpcConfig().createServer(port, activeLimit, waitingLimit);
        server.start();
        System.out.println("Consumer gRPC server listening on port " + port);
        try (var web = SpringApplication.run(Main.class, "--server.port=8080")) {
            server.awaitTermination();
        } finally {
            server.shutdown();
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
