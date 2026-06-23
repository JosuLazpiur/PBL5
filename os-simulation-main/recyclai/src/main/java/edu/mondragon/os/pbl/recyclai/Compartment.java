package edu.mondragon.os.pbl.recyclai;

/**
 * COMPARTMENT: DATA OBJECT
 * Note: Synchronization is handled by the parent 'Bin' class (The Monitor),
 * so this class does not need internal locks.
 */

public class Compartment {
    private  TrashType type;
    private int level = 0;
    private  int capacity;


    public Compartment(TrashType type, int capacity) {
        this.type = type;
        this.capacity = capacity;
    }

    public boolean isFull() {
        return level >= capacity;
    }

    public void add() {
        if (level < capacity) {
            level++;
        }
    }

    public void empty() {
        level = 0;
    }

    public int getLevel() {
        return level;
    }

    public int getCapacity() {
        return capacity;
    }

    public TrashType getType() {
        return type;
    }
}
