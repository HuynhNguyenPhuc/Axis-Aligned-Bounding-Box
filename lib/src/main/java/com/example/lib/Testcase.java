package com.example.lib;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class Testcase {
    public static void main(String[] args) {
        Testcase test = new Testcase();
        String filename = "app/src/main/assets/testcases.txt";

        test.generateTestcase(filename, 100, 5);
    }

    private void readTestcase(String filename) {
        try {
            InputStream inputStream = new FileInputStream(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                // TODO: process line
            }

            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateTestcase(String filename, int numAABBs, int numRays) {
        Random random = new Random();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < numAABBs; i++) {
                double min_x = getRandomCoordinate(random);
                double min_y = getRandomCoordinate(random);
                double min_z = getRandomCoordinate(random);
                double max_x = min_x + getRandomExtent(random);
                double max_y = min_y + getRandomExtent(random);
                double max_z = min_z + getRandomExtent(random);

                writer.write(String.format("aabb %.2f %.2f %.2f %.2f %.2f %.2f%n",
                        min_x, min_y, min_z, max_x, max_y, max_z));
            }

            for (int i = 0; i < numRays; i++) {
                double ray_o_x = getRandomCoordinate(random);
                double ray_o_y = getRandomCoordinate(random);
                double ray_o_z = getRandomCoordinate(random);
                double ray_direction_x = getRandomDirection(random);
                double ray_direction_y = getRandomDirection(random);
                double ray_direction_z = getRandomDirection(random);

                writer.write(String.format("ray %.2f %.2f %.2f %.2f %.2f %.2f%n",
                        ray_o_x, ray_o_y, ray_o_z, ray_direction_x, ray_direction_y, ray_direction_z));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getRandomCoordinate(Random random) {
        return -100 + 200 * random.nextDouble();
    }

    private double getRandomExtent(Random random) {
        return 1 + 100 * random.nextDouble();
    }

    private double getRandomDirection(Random random) {
        return -1 + 2 * random.nextDouble();
    }
}