package com.gregmcnew.android.pax;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

// IMPORTANT: For this project to build, you must first
// copy all files in: pax/src/com/gregmcnew/android/pax
//                to: pax/aitest/src/com/gregmcnew/android/pax
//
// I'm sure there's a cleaner way to get this to work,
// but in the meantime this is fine.

public class AITest {

    private static final String FILENAME = "population.txt";

    // The size of the population of hypotheses.
    private static final int P = 10;
    private static final int FIGHTS = 10;

    Game mGame;

    public AITest() {
        mGame = new Game(0);

        for (Player player : mGame.mPlayers) {
            player.setAI(true);
        }
    }

    // Returns a score from 0 to 2, where 1+ is a win and 2 is perfect.
    private float runGame(Hypothesis blue, Hypothesis red) {

        mGame.mPlayers[0].setAIDifficulty(AI.Difficulty.CHEATER);
        mGame.mPlayers[1].setAIDifficulty(AI.Difficulty.CHEATER);

        mGame.mPlayers[0].setAIWeights(blue.getWeights());
        mGame.mPlayers[1].setAIWeights(red.getWeights());

        mGame.restart();

        Game.State state;

        do {
            mGame.update(25);
            state = mGame.getState();
        } while (Game.State.IN_PROGRESS == state);
        mUpgrades += mGame.mPlayers[0].numUpgrades();

        float score = 0.5f;

        switch (state) {
            case RED_WINS:
                // Our score is the percentage of health the enemy factory has left, negated.
                Entity factory = mGame.mPlayers[1].mEntities[Entity.FACTORY].get(0);
                score -= 0.5f * factory.health / Factory.HEALTH;
                red.wins++;
                blue.losses++;
                break;
            case BLUE_WINS:
                // Our score is the percentage of health our factory has left.
                factory = mGame.mPlayers[0].mEntities[Entity.FACTORY].get(0);
                score += 0.5f * factory.health / Factory.HEALTH;
                red.losses++;
                blue.wins++;
                break;
        }

        return score;
    }


    private int mUpgrades;

    private class Hypothesis implements Comparable<Hypothesis> {

        public Hypothesis() {
            AIWeights aiw = new AIWeights();
            aiw.randomize();
            init(0, aiw);
        }
        public Hypothesis(float score, AIWeights weights) {
            init(score, weights);
        }

        private void init(float score, AIWeights weights) {
            this.score = score;
            this.weights = weights;
            this.age = 0;
        }


        public int compareTo(Hypothesis other) {
            return Float.compare(score, other.score);
        }

        @Override
        public int hashCode() {
            String str = "";
            for (int i = 0; i < weights.w.length; i++) {
                str += String.format("%.4f ", weights.w[i]);
            }
            return str.hashCode();
        }

        @Override
        public String toString() {
            String str = String.format("%.3f %2d-%2d (%d %s)", score, wins, losses, age, Integer.toHexString(hashCode()).substring(0, 4));
            for (int i = 0; i < weights.w.length; i++) {
                str += String.format(" %.4f", weights.w[i]);
            }
            return str;
        }

        public AIWeights getWeights() {
            return weights.clone();
        }

        private int age;
        private float score;
        private AIWeights weights;

        public int wins;
        public int losses;
    }

    public void run() throws IOException {

        Hypothesis[] population = new Hypothesis[P];

        // Read the population from a text file, and create new members if needed.
        {
            int i = 0;

            File populationFile = new File(FILENAME);
            if (populationFile.exists()) {
                Scanner scanner = new Scanner(populationFile);

                int j = 0;
                AIWeights aiw = new AIWeights();

                while (scanner.hasNextFloat()) {
                    aiw.w[j] = scanner.nextFloat();
                    j++;
                    if (j == AIWeights.NUM_WEIGHTS) {

                        // chew up the score
                        float score = scanner.nextFloat();
                        j = 0;

                        if (i < P) {
                            population[i++] = new Hypothesis(score, aiw);
                            aiw = new AIWeights();
                        }
                    }
                }
            }

            if (i > 0) {
                System.out.println(String.format("read %d members from text file", i));
            }
            for (; i < P; i++) {
                population[i] = new Hypothesis();
            }
        }

        int generation = 0;

        System.out.println(String.format("population: %d", P));
        System.out.println();

        // After each generation, the worst AIs are replaced by random ones.
        while (true) {

            float[] rrScores = new float[P];
            generation += 1;

            for (int i = 0; i < P; i++) {
                population[i].wins = 0;
                population[i].losses = 0;
            }

            System.out.println(String.format("generation %d:", generation));
            for (int i = 0; i < P; i++) {
                for (int j = 0; j < P; j++) {
                    if (i < j) {
                        float score = 0;
                        for (int k = 0; k < FIGHTS; k++) {
                            Hypothesis a = population[i];
                            Hypothesis b = population[j];
                            if (k % 2 == 0) {
                                score += runGame(a, b);
                            }
                            else {
                                score += 1 - runGame(b, a);
                            }
                        }
                        score /= FIGHTS;
                        rrScores[i] += score;
                        rrScores[j] += 1 - score;
                        System.out.print(String.format("%.3f ", score));
                    }
                    else {
                        System.out.print("      ");
                    }
                }

                population[i].score = rrScores[i] / (P - 1);
                System.out.println("     " + population[i]);
            }

            //
            // Print the new population to a text file.
            //
            PrintStream ps = new PrintStream(new File(FILENAME));
            for (int i = 0; i < P; i++) {
                for (int j = 0; j < AIWeights.NUM_WEIGHTS; j++) {
                    ps.print(String.format("% .8f ", population[i].weights.w[j]));
                }
                ps.println(String.format("    % .8f", population[i].score));
            }
            ps.close();

            Arrays.sort(population);

            int children = 2;

            for (int i = 0; i < P; i++) {
                if (i < children) {
                    int i1 = children + Game.sRandom.nextInt(P - children);
                    int i2 = children + Game.sRandom.nextInt(P - children - 1);
                    if (i2 >= i1) i2++;
                    Hypothesis h = new Hypothesis();
                    h.weights = breed(population[i1].weights, population[i2].weights);
                    h.age = (population[i1].age + population[i2].age) / 2 + 1;
                    population[i] = h;
                }
                else if (i < 5) {
                    population[i] = new Hypothesis();
                }
                else {
                    population[i].age++;
                }
            }
            System.out.println();
        }
    }

    private AIWeights breed(AIWeights parent1, AIWeights parent2) {
        AIWeights child = new AIWeights();

        float f = Game.sRandom.nextFloat() * 0.33f + 0.33f;
        for (int i = 0; i < AIWeights.NUM_WEIGHTS; i++) {
            child.w[i] = (f * parent1.w[i]) + ((1 - f) * parent2.w[i]);
        }

        return child;
    }

    public static void main(String[] args) {

        Constants.sGameSpeed = 1f;

        AITest test = new AITest();
        try {
            test.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
