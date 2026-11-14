package media.upload;

final class SimulationProducer implements Runnable {
    private final QueueSimulation simulation;
    private final String filename;

    SimulationProducer(QueueSimulation simulation, String filename) {
        this.simulation = simulation;
        this.filename = filename;
    }

    @Override
    public void run() {
        simulation.submit(filename);
    }
}

