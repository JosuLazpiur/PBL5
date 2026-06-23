package edu.mondragon.os.pbl.recyclai;

import java.util.List;
import java.util.Random;

/**
 * PERSON: CLIENT THREAD
 * * Represents a user interacting with the system.
 * Multiple Person threads run concurrently, creating contention for the shared Bins.
 * They attempt to enter the Monitor (Bin) to deposit trash.
 */

public class Person extends Thread {
    private String name;
    private List<TrashType> itemsToDispose;
    private TrashBinSystem system;
    private Random rand = new Random();
    private Bin assignedBin;

    public Person(String name, List<TrashType> items, TrashBinSystem system) {
        this.name = name;
        this.itemsToDispose = items;
        this.system = system;
        this.assignedBin = system.getRandomBin();
    }

     @Override
    public void run() {
        try {
            System.out.printf("[%s] %s assigned to %s. Starting disposal route.%n", 
                    system.time(), name, assignedBin.getId());
            
            for (TrashType trash : itemsToDispose) {
                // Try to deposit.
                // This call might block the thread if the Bin monitor is locked or conditions are not met.
                assignedBin.deposit(trash, this.name);
                
                // Simulate travel time or delay between items SONAR fix casting to L
                Thread.sleep(50L + rand.nextInt(100));
            }
            System.out.printf("[%s] %s has finished disposing all trash in %s.%n", system.time(), name, assignedBin.getId());
        } catch (InterruptedException e) {
            System.out.println(name + " interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}