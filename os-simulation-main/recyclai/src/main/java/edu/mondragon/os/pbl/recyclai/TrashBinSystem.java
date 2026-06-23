package edu.mondragon.os.pbl.recyclai;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * CORE SYSTEM: SYNCHRONIZATION VIA MESSAGE PASSING (Rubric Level 3)
 * * This class implements the Producer-Consumer pattern using Message Passing.
 * Instead of using shared memory protected by manual Monitors
 * (Locks/Condition),
 * it uses a thread-safe BlockingQueue to coordinate Bins and the Collector.
 * 
 *  * * Roles:
 * - Producers: The Bins (when they become full).
 * - Consumer: The GarbageCollector thread.
 * - Message: The 'Bin' object reference passed through the queue.
 */
 
public class TrashBinSystem {
    private List<Bin> binList = new ArrayList<>();

    // [MESSAGE PASSING CHANNEL]
    // BlockingQueue is a thread-safe structure that handles concurrency internally.
    // It handles concurrency internally using Locks and Condition Variables,
    // abstracting the synchronization complexity from the user.
    private BlockingQueue<Bin> collectionQueue = new LinkedBlockingQueue<>();

    // [POISON PILL PATTERN]
    // A special object used to signal the Consumer thread to terminate gracefully.
    // Since take() blocks indefinitely, we need a message to "unblock" and stop it.
    private final Bin poisonPill;

    private boolean shutdown = false;

    private final Random rand = new Random();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public TrashBinSystem(int numBins, int capacityPerType) {
        // Initialize the termination signal (dummy bin with ID "SHUTDOWN_SIGNAL")
        this.poisonPill = new Bin("SHUTDOWN_SIGNAL", 0, this);

        for (int i = 1; i <= numBins; i++) {
            String binId = "BIN_" + i;
            binList.add(new Bin(binId, capacityPerType, this));
        }
    }

    public Bin getRandomBin() {
        return binList.get(rand.nextInt(binList.size()));
    }

    public int getNumBins() {
        return binList.size();
    }

    public Bin getBin(int idx) {
        return binList.get(idx % binList.size());
    }

    /**
     * [PRODUCER]
     * Called by Bins when a compartment is full.
     * It sends a message (the Bin object) to the shared queue.
     * * Concurrency: Thread-safe. 'put' handles internal locking.
     * * If the queue were bounded (limited size) and full, this thread would block.
     */
    public void requestCollection(Bin bin) {
        try {
            System.out.printf("[%s] SYSTEM: Request received from %s. Sending message to queue.%n", time(),
                    bin.getId());
            collectionQueue.put(bin);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * [CONSUMER]
     * Called by GarbageCollector to retrieve the next task.
     * * Concurrency: Uses 'take()' which BLOCKS the thread if the queue is empty.
     * This eliminates "Busy Waiting" (Level 2 requirement) and simplifies
     * synchronization.
     */
    public Bin waitForNextJob() throws InterruptedException {
        // BLOCKS here until a message is available.
        Bin task = collectionQueue.take();

        // Check if the message is the Poison Pill (Shutdown signal)
        if (task == poisonPill) {
            return null; // Return null to break the Collector's loop
        }

        return task;
    }

    public void stopSystem() {
        shutdown = true;
        try {
            System.out.printf("[%s] SYSTEM: Sending shutdown signal (Poison Pill) to Collector.%n", time());
            // We send the Poison Pill to unblock the consumer if it's waiting, ensuring a clean exit.
            collectionQueue.put(poisonPill);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public String time() {
        return LocalDateTime.now().format(FORMATTER);
    }
}