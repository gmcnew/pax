package com.gregmcnew.android.pax;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

// IMPORTANT: For this project to build, you must first
// copy all files in: pax/src/com/gregmcnew/android/pax
//                to: pax/aitest/src/com/gregmcnew/android/pax
//
// I'm sure there's a cleaner way to get this to work,
// but in the meantime this is fine.

public class AITest {

    private static final String FILENAME = "population.txt";

    // The size of the population of hypotheses.
    private static final int P = 30;

    // The percentage of the population to be replaced by crossover at each new generation.
    private static final float CROSSOVER = 0.6f;

    // The percentage of the population to be mutated at each new generation.
    private static final float MUTATION = 0.01f;

    Game mGame;

    public AITest() {
        mGame = new Game(0);
    }

    // Returns a score from 0 to 2, where 1+ is a win and 2 is perfect.
    private float runGame(AIWeights weights) {

        mGame.mPlayers[0].setAIDifficulty(AI.Difficulty.CHEATER);
        mGame.mPlayers[1].setAIDifficulty(AI.Difficulty.CHEATER);

        mGame.mPlayers[0].setAIWeights(weights);

        for (Player player : mGame.mPlayers) {
            player.setAI(true);
        }

        mGame.restart();

        Game.State state;

        do {
            mGame.update(25);
            state = mGame.getState();
        } while (Game.State.IN_PROGRESS == state);

        float score = 0;

        switch (state) {
            case RED_WINS:
                // Our score is the percentage of health the enemy factory has left, negated.
                Entity redFactory = mGame.mPlayers[1].mEntities[Entity.FACTORY].get(0);
                score = -(float) redFactory.health / Factory.HEALTH;
                break;
            case BLUE_WINS:
                // Our score is the percentage of health our factory has left.
                Entity blueFactory = mGame.mPlayers[0].mEntities[Entity.FACTORY].get(0);
                score = (float) blueFactory.health / (float) Factory.HEALTH;
                break;
        }

        mUpgrades += mGame.mPlayers[0].numUpgrades();

        return score + 1;
    }

    private static int biasedSelect(float scoresSum, float[] scores) {

        double toSelect = Math.random() * scoresSum;

        int selectedIndex = -1;
        double selectCounter = 0;

        for (int j = 0; j < P; j++) {
            selectCounter += scores[j];
            if (selectCounter >= toSelect) {
                selectedIndex = j;
                break;
            }
        }

        return selectedIndex;
    }

    private int mUpgrades;

    public void run() throws IOException {

        AIWeights[] population = new AIWeights[P];
        float[] scores = new float[P];

        AIWeights[] newPopulation = new AIWeights[P];

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
                        if (i < P) {
                            population[i++] = aiw;
                            aiw = new AIWeights();
                        }
                        // chew up the score
                        scanner.nextFloat();
                        j = 0;
                    }
                }
            }

            if (i > 0) {
                System.out.println(String.format("read %d members from text file", i));
            }
            for (; i < P; i++) {
                AIWeights aiw = new AIWeights();
                aiw.randomize();
                population[i] = aiw;
            }
        }

        int generation = 0;

        int numToCrossover = (int) (CROSSOVER * P);

        // numToCrossover must be an even number.
        numToCrossover /= 2;
        numToCrossover *= 2;

        int numToSelect = P - numToCrossover;
        int numToMutate = (int) (MUTATION * P);

        System.out.println(String.format("Population: %d (select %d, crossover %d, mutate %d)", P, numToSelect, numToCrossover, numToMutate));

        while (true) {

            //
            // Evaluate the fitness of each set of weights.
            //
            float scoresSum = 0;
            mUpgrades = 0;
            for (int i = 0; i < P; i++) {
                for (int j = 0; j < 1; j++) {
                    float score = runGame(population[i]);
                    scores[i] = score;
                    //System.out.println(String.format("score: %f", score));
                    scoresSum += score;
                }
            }

            float maxScore = scores[0];
            float minScore = scores[0];
            for (int i = 1; i < P; i++) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i];
                }
                else if (scores[i] < minScore) {
                    minScore = scores[i];
                }
            }

            System.out.println(String.format("generation %3d: average %f, max %f (%3d upgrades)",
                    generation, scoresSum / P, maxScore, mUpgrades));

            for (int i = 0; i < P; i++) {
                for (int j = 0; j < AIWeights.NUM_WEIGHTS; j++) {
                    System.out.print(String.format("% .4f", population[i].w[j]));
                    System.out.print(" ");

                    if (j == AIWeights.NUM_WEIGHTS - 1) {
                        System.out.print(String.format("    % .4f", scores[i]));
                    }
                }
                System.out.println();
            }

            // Prior to selection, scale all scores to the range [0..1].
            float range = maxScore - minScore;
            if (range > 0) {
                scoresSum -= minScore * P;
                scoresSum /= range;
                for (int i = 0; i < P; i++) {
                    scores[i] -= minScore;
                    scores[i] /= range;
                }
            }

            //
            // Select, breed, and mutate weights for the next generation.
            //

            // new index
            int ni = 0;

            float[] newPopScores = new float[P];
            boolean[] selected = new boolean[P];
            for (int i = 0; i < P; i++) {
                selected[i] = false;
            }

            for (int i = 0; i < numToSelect; i++) {
                int selectedIndex = biasedSelect(scoresSum, scores);

                //System.out.println(String.format("selected a weights-set with score %f", scores[selectedIndex]));

                newPopScores[ni] = scores[selectedIndex];
                newPopulation[ni++] = population[selectedIndex];
            }

            for (int i = 0; i < numToCrossover / 2; i++) {
                int motherIndex = biasedSelect(scoresSum, scores);
                int fatherIndex = biasedSelect(scoresSum, scores);

                for (int j = 0; j < 2; j++) {
                    AIWeights child = population[motherIndex].breed(population[fatherIndex]);
                    newPopScores[ni] = 0;
                    newPopulation[ni++] = child;
                }

				/*
				 * System.out.println(String.format(
				 * "applied crossover to parents with scores %f and %f",
				 * scores[motherIndex], scores[fatherIndex]));
				 */
            }

            for (int i = 0; i < numToMutate; i++) {
                int mutateIndex = Game.sRandom.nextInt(P);
                newPopulation[mutateIndex].mutate();
                newPopScores[mutateIndex] = 0;
            }

            //
            // Move on to the next generation.
            //

            generation++;
            AIWeights[] temp = population;
            population = newPopulation;
            newPopulation = temp;

            //
            // Print the new population to a text file.
            //
            PrintStream ps = new PrintStream(new File(FILENAME));
            for (int i = 0; i < P; i++) {
                for (int j = 0; j < AIWeights.NUM_WEIGHTS; j++) {
                    ps.print(String.format("% .8f", population[i].w[j]));
                    //if (j < AIWeights.NUM_WEIGHTS - 1) {
                    ps.print(" ");
                    //}

                    if (j == AIWeights.NUM_WEIGHTS - 1) {
                        ps.print(String.format("    % .8f", newPopScores[i]));
                    }
                }
                ps.println();
            }
            ps.close();
        }
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
