package media.upload;

final class SimulationWorker implements Runnable {
    private final QueueSimulation simulation;
    private final String firstJob;
    private final int delayMillis;

    SimulationWorker(QueueSimulation simulation, String firstJob, int delayMillis) {
        this.simulation = simulation;
        this.firstJob = firstJob;
        this.delayMillis = delayMillis;
    }

    @Override
    public void run() {
        String filename = firstJob;
        while (filename != null) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException interrupted) {
                System.out.println("INTERRUPTED " + filename);
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println("COMPLETED " + filename);
            filename = simulation.takeNextJob();
        }
    }
}
