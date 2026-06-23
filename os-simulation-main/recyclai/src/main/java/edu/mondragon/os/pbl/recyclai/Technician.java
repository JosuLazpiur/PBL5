package edu.mondragon.os.pbl.recyclai;

// SONAR FIX: Removed unused Random import

/**
 * TECHNICIAN: PRIORITY INTERRUPTER THREAD
 * The Technician simulates random breakdowns of entire Bins.
 */
public class Technician extends Thread {
    private TrashBinSystem system;

    // SONAR FIX: Constants are static final and proper naming
    private static final int POLLING_INTERVAL = 1000;
    private static final int SLEEP_TIME = 5000; 

    public Technician(TrashBinSystem system) {
        this.system = system;
    }

    @Override
    public void run() {
        try {
            while (!system.isShutdown()) {
                // 1. Low frequency: Sleeps for a long time between incidents
                Thread.sleep(POLLING_INTERVAL);

                if (system.isShutdown())
                    break;

                for (int i = 0; i < system.getNumBins(); i++) {
                    // 2. Pick a random Bin to break
                    Bin targetBin = system.getBin(i);

                    // synchronous GET of the Backend
                    boolean isOperativeInCloud = BackendService.getBinOperativeStatus(targetBin.getId());

                    if (!isOperativeInCloud && !targetBin.isBroken()) {
                        System.out.printf("[%s] ALERT: WEB Alert received! %s has reported an incident%n",
                                system.time(), targetBin.getId());

                        // 3. Break the bin
                        targetBin.setBroken(true);

                        // 4. Simulate repair time
                        System.out.printf("[%s] TECHNICIAN: Repairing %s... (Hammering sounds)%n", system.time(),
                                targetBin.getId());
                        Thread.sleep(SLEEP_TIME);

                        // 5. Fixed
                        targetBin.setBroken(false);
                        // Update the variable Operative = true
                        BackendService.updateBinStatus(targetBin.getId(), true);
                        System.out.printf("[%s] TECHNICIAN: %s is fixed and OPERATIVE.%n", system.time(),
                                targetBin.getId());

                        // INTEGRATION: Send Log to Backend (FIXED)
                        BackendService.sendLog(targetBin.getId(), "Incident resolved by the Technician");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("[%s] TECHNICIAN: Shift ended.%n", system.time());
    }
}