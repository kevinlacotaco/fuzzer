package utils;

import java.util.List;
import java.util.Random;

public class RandomFuzzer {

    private static Random randomNumGen = new Random(
            System.currentTimeMillis());

    public static String getRandomString() {
        List<String[]> allVectors = FuzzVectors.getAllVectors();

        int attackClass = randomNumGen.nextInt(allVectors.size());

        String[] classVector = allVectors.get(attackClass);

        int specificStringIndex = randomNumGen.nextInt(classVector.length);

        return classVector[specificStringIndex];
    }
}
