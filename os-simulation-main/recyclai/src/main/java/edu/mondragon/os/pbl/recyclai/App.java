package edu.mondragon.os.pbl.recyclai;

/**
 * MAIN ENTRY POINT
 * * Orchestrates the simulation lifecycle:
 * 1. Initializes the System and Shared Resources (Bins).
 * 2. Creates and starts Actor threads (Collector, Technician, Persons).
 * 3. Manages graceful shutdown.
 */

public class App {

    private Person[] persons;
    private GarbageCollector collector;
    private Technician technician;
    private TrashBinSystem system;

    // Num variables
    public static final int NUM_PERSONS = 10;
    public static final int NUM_BINS = 3; 
    public static final int CAPACITY_PER_TYPE = 4;
    public static final int TRASH_PER_PERSON = 15;

    public App() {
        system = new TrashBinSystem(NUM_BINS, CAPACITY_PER_TYPE);

        persons = new Person[NUM_PERSONS];
        for (int i = 0; i < NUM_PERSONS; i++) {
            persons[i] = new Person("Person " + i, TrashGenerator.randomList(TRASH_PER_PERSON), system);
        }

        collector = new GarbageCollector(system);
        technician = new Technician(system);
    }

    public void startSimulation() {
        collector.setName("Collector");
        collector.start();
        
        technician.setName("Technician");
        technician.start();

        for (Person p : persons) {
            p.start();
        }
    }

    /**
     * Synchronization Barrier.
     * Waits for all user threads (Persons) to complete their execution using 'join()'.
     * Once all users are done, it signals the service threads to stop.
     */
    public void waitEndOfThreads() {
        for (Person p : persons) {
            try { 
                // Blocks the main thread until thread 'p' terminates
                p.join(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.printf("[%s] MAIN: All persons have finished.%n", system.time());

        // 1. Send Poison Pill to Collector (Message Passing termination)
        system.stopSystem();

        // 2. Interrupt Technician (since it might be sleeping in polling loop)
        technician.interrupt(); 
        
        try { 
            collector.join(); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try { 
            technician.join(); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("--- SIMULATION END ---");
    }

    public static void main(String[] args) {
        App app = new App();
        app.startSimulation();
        app.waitEndOfThreads();
    }
}