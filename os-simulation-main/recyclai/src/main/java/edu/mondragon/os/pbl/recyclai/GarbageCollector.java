package edu.mondragon.os.pbl.recyclai;

public class GarbageCollector extends Thread {
    private TrashBinSystem system;

/**
 * GARBAGE COLLECTOR: CONSUMER THREAD
 * * This thread represents the service vehicle.
 * It implements the Consumer role in the Producer-Consumer pattern.
 * It waits for messages (Bins) from the TrashBinSystem queue and processes them sequentially.
 */
    public GarbageCollector(TrashBinSystem system) {
        this.system = system;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // 1. Consume Message (Blocking Call)
                // This thread sleeps here if the queue is empty.
                Bin targetBin = system.waitForNextJob();
                
                if (targetBin == null) break;

                System.out.printf("[%s] COLLECTOR: New job assigned! Moving to %s.%n", 
                system.time(), targetBin.getId());

                
                // 2. Give priority to the Collector
                targetBin.setUnderMaintenance(true);

                System.out.printf("[%s] COLLECTOR: Emptying compartments in %s...%n", system.time(), targetBin.getId());

                // INTEGRATION: Send Log to Backend (ACTIVITY)
                BackendService.sendLog(targetBin.getId(), "Garbage Collector started emptying the bin.");

                Thread.sleep(1000); //work time simulation

                targetBin.emptyAllCompartments();
                System.out.printf("[%s] COLLECTOR: Finished %s.%n", system.time(), targetBin.getId());

                // INTEGRATION: Send Log to Backend (FINISHED)
                BackendService.sendLog(targetBin.getId(), "Garbage Collector finished. Bin is empty.");

                // 3. Release Access
                targetBin.setUnderMaintenance(false);
                
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("[%s] COLLECTOR: Shift ended.%n", system.time());
    }
}