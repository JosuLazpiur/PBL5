package edu.mondragon.os.pbl.recyclai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class to generate random trash lists for users.
 */

public class TrashGenerator {
    
    private static final Random random = new Random();

    private TrashGenerator() { 
        throw new IllegalStateException("Utility class"); //only for sonarQube
    }

    public static List<TrashType> randomList(int n) {
        List<TrashType> list = new ArrayList<>();
        TrashType[] vals = TrashType.values();
        for (int i = 0; i < n; i++) {
            list.add(vals[random.nextInt(vals.length)]);
        }
        return list;
    }
}