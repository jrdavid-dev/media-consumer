package media.upload;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class QueueSimulation {
    private final List<String> assigned = new ArrayList<>();
    private final Queue<String> waiting = new ArrayDeque<>();
    private final int workerCount;
    private final int waitingCapacity;
    private final int delayMillis;

    private QueueSimulation(int workerCount, int waitingCapacity, int delayMillis) {
        this.workerCount = workerCount;
        this.waitingCapacity = waitingCapacity;
        this.delayMillis = delayMillis;
    }

    synchronized void submit(String filename) {
        if (assigned.size() < workerCount) {
            assigned.add(filename);
            System.out.println("READY " + filename);
        } else if (waiting.size() < waitingCapacity) {
            waiting.add(filename);
            System.out.println("QUEUED " + filename);
        } else {
            System.out.println("REJECTED " + filename);
        }
    }

    synchronized String takeNextJob() {
        String filename = waiting.poll();
        if (filename != null) {
            System.out.println("READY " + filename);
        }
        return filename;
    }

    private void run(int producerCount) throws InterruptedException {
        List<Thread> producers = new ArrayList<>();
        for (int number = 1; number <= producerCount; number++) {
            String filename = "video-" + number + ".mp4";
            Thread producer = new Thread(new SimulationProducer(this, filename), "producer-" + number);
            producers.add(producer);
            producer.start();
        }

        // Finish admitting the first wave before workers start, so the demo reliably fills the queue.
        for (Thread producer : producers) {
            producer.join();
        }

        List<Thread> workers = new ArrayList<>();
        for (String filename : assigned) {
            Thread worker = new Thread(new SimulationWorker(this, filename, delayMillis),
                    "worker-" + (workers.size() + 1));
            workers.add(worker);
            worker.start();
        }

        for (Thread worker : workers) {
            worker.join();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            if (args.length == 0) {
                args = new String[]{"6", "2", "3", "200"};
            }
            if (args.length != 4) {
                throw new IllegalArgumentException();
            }
            int producers = Integer.parseInt(args[0]);
            int workers = Integer.parseInt(args[1]);
            int waiting = Integer.parseInt(args[2]);
            int delay = Integer.parseInt(args[3]);
            if (producers < 1 || workers < 1 || waiting < 0 || delay < 0) {
                throw new IllegalArgumentException();
            }
            new QueueSimulation(workers, waiting, delay).run(producers);
        } catch (IllegalArgumentException invalidInput) {
            System.err.println("Usage: QueueSimulation <p >= 1> <c >= 1> <q >= 0> <delayMillis >= 0>");
            System.exit(2);
        }
    }
}

