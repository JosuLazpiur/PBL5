package edu.mondragon.os.pbl.recyclai;

import java.util.EnumMap;
import java.util.concurrent.locks.*;

/**
 * BIN: MONITOR PATTERN IMPLEMENTATION
 * * This class acts as a Monitor (Shared Resource). It encapsulates:
 * 1. Shared Data (Compartments state).
 * 2. Mutual Exclusion (ReentrantLock).
 * 3. Conditional Synchronization (Condition variables).
 * * It ensures that multiple threads (Persons, Technician, Collector) can
 * access
 * the bin safely without Race Conditions.
 */

public class Bin {
    private String id;
    private EnumMap<TrashType, Compartment> compartments = new EnumMap<>(TrashType.class);
    private TrashBinSystem system;

    // --- SHARED STATE VARIABLES ---
    private int fullCompartmentCount;
    private boolean isUnderMaintenance;
    private boolean isBroken;
    private boolean isQueuedForCollection;
    private boolean serviceRequested;

    private Lock mutex = new ReentrantLock(true);
    private Condition binOperational = mutex.newCondition();

    // Condition Variable 2: Specific Wait Queues per Trash Type.
    // Granular locking optimization: A user waiting for PLASTIC does not block a
    // user waiting for GLASS.
    private EnumMap<TrashType, Condition> spaceAvailable = new EnumMap<>(TrashType.class);

    public Bin(String id, int capacityPerType, TrashBinSystem system) {
        this.id = id;
        this.system = system;
        this.fullCompartmentCount = 0;

        for (TrashType t : TrashType.values()) {
            compartments.put(t, new Compartment(t, capacityPerType));
            spaceAvailable.put(t, mutex.newCondition());
        }
    }

    public String getId() {
        return id;
    }

    /**
     * CRITICAL SECTION: Deposit Trash
     * This method implements the logic for a thread to enter the monitor and modify
     * state.
     * It handles blocking logic if conditions are not met.
     */
    public void deposit(TrashType type, String personName) throws InterruptedException {
        mutex.lock();
        try {
            waitForAvailability(type, personName);

            // Action: Deposit garbage
            Compartment c = compartments.get(type);
            c.add();
            System.out.printf("[%s] %s has thrown %s in %s (id=%s). Level=%d/%d%n",
                    system.time(), personName, type, id, type, c.getLevel(), c.getCapacity());

            // If full, notify the system (Message Passing) and update Backend (Integration)
            if (c.isFull()) {
                System.out.printf("[%s] ALERT: %s in %s is now FULL.%n", system.time(), type, id);
                BackendService.sendAlert(this.id, "Compartment Full", type + " compartment is full.");
                reportFullCompartment();
            }
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Helper method to handle conditional waiting logic (The "Wait Loop").
     */
    private void waitForAvailability(TrashType type, String personName) throws InterruptedException {
        while (shouldWait(type)) {
            if (isGlobalBlock()) {
                String status = isBroken ? "BROKEN" : "COLLECTOR is working";
                String reason = serviceRequested ? "Service Vehicle Priority" : status;

                System.out.printf("[%s] %s waits for %s in %s (%s).%n",
                        system.time(), personName, type, id, reason);

                binOperational.await();
            } else {
                Compartment c = compartments.get(type);
                System.out.printf("[%s] %s wants to throw %s in %s but it is FULL (%d/%d).%n",
                        system.time(), personName, type, id, c.getLevel(), c.getCapacity());

                spaceAvailable.get(type).await();
            }
        }
    }

    private boolean shouldWait(TrashType type) {
        return isGlobalBlock() || compartments.get(type).isFull();
    }

    private boolean isGlobalBlock() {
        // Priority logic: Broken or Maintenance stops everyone.
        return isBroken || isUnderMaintenance || serviceRequested;
    }

    private void reportFullCompartment() {
        fullCompartmentCount++;
        if (fullCompartmentCount >= 1 && !isQueuedForCollection) {
            isQueuedForCollection = true;
            System.out.printf("[%s] SYSTEM: Single compartment full in %s. Requesting collection.%n",
                    system.time(), this.getId());
            // Delegate to System (Message Passing)
            system.requestCollection(this);
        }
    }

    public void emptyAllCompartments() {
        mutex.lock();
        try {
            for (Compartment c : compartments.values()) {
                c.empty();
                // Signal: Wake up all threads waiting on this specific trash type condition.
                spaceAvailable.get(c.getType()).signalAll();
            }
        } finally {
            mutex.unlock();
        }
    }

    public void setBroken(boolean broken) throws InterruptedException {
        mutex.lock();
        try {
            if (broken) {
                this.serviceRequested = true;
                while (isUnderMaintenance) {
                    binOperational.await();
                }
                this.isBroken = true;
                this.serviceRequested = false;
            } else {
                this.isBroken = false;
                // Wake up ALL threads (Users and Collector) waiting on the global condition
                binOperational.signalAll();
            }
        } finally {
            mutex.unlock();
        }
    }

    public void setUnderMaintenance(boolean active) throws InterruptedException {
        mutex.lock();
        try {
            if (active) {
                this.serviceRequested = true;
                while (isBroken) {
                    binOperational.await();
                }
                this.isUnderMaintenance = true;
                this.serviceRequested = false;
            } else {
                this.isUnderMaintenance = false;
                this.isQueuedForCollection = false;
                this.fullCompartmentCount = 0;
                // Signal that Bin is operative again
                binOperational.signalAll();
            }
        } finally {
            mutex.unlock();
        }
    }

    public boolean isBroken() {
        mutex.lock();
        try {
            return isBroken;
        } finally {
            mutex.unlock();
        }
    }

    public boolean isUnderMaintenance() {
        mutex.lock();
        try {
            return isUnderMaintenance;
        } finally {
            mutex.unlock();
        }
    }

}