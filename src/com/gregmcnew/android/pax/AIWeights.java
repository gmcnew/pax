package com.gregmcnew.android.pax;

public class AIWeights {
	
	// These weights were determined through experimentation. Over 3000 games
	// were played between the null AI (with weights of 0) and an AI with random
	// weights (re-randomized in each round). After each round, the random AI
	// was assigned a score: if they won, their score was the percentage of
	// health their factory had left. If they lost, their score was the
	// percentage of health the enemy's factory had left, negated. The resulting
	// scores and weights were run through linear regression to come up with the
	// following optimal weights.
	
	// The final weight [0..1] indicates the importance of enemy health. 0 means
	// a nearly-dead enemy is treated as equal to a full-health enemy. 1 means a
	// nearly-dead enemy is almost ignored.
	private static final float OPTIMAL_WEIGHTS[] = {
			0.6277f, -0.2779f,
			1.1031f, -0.1376f,
			1.0f,	 0f,
			0f,
			};

	private static final int NUM_WEIGHTS = OPTIMAL_WEIGHTS.length;
	
	public AIWeights() {
		w = new float[NUM_WEIGHTS];
		reset();
	}
	
	public static final int FIGHTER_X    = 0;
	public static final int FIGHTER_C    = 1;
	public static final int BOMBER_X     = 2;
	public static final int BOMBER_C     = 3;
	public static final int FRIGATE_X    = 4;
	public static final int FRIGATE_C    = 5;
	public static final int ENEMY_HEALTH = 6;
	
	public void reset() {
		for (int i = 0; i < NUM_WEIGHTS; i++) {
			w[i] = OPTIMAL_WEIGHTS[i];
		}
	}
	
	public void randomize() {
		// Make sure that all *X weights are positive.
		w[FIGHTER_X]    =  Game.sRandom.nextFloat();
		w[FIGHTER_C]    = (Game.sRandom.nextFloat() - 0.5f) * 2;
		w[BOMBER_X]     =  Game.sRandom.nextFloat();
		w[BOMBER_C]     = (Game.sRandom.nextFloat() - 0.5f) * 2;
		w[FRIGATE_X]    =  Game.sRandom.nextFloat();
		w[FRIGATE_C]    = 0;
		w[ENEMY_HEALTH] = 0;
	}
	
	public AIWeights clone() {
		AIWeights clone = new AIWeights();
		for (int i = 0; i < NUM_WEIGHTS; i++) {
			clone.w[i] = w[i];
		}
		return clone;
	}
	
	public float[] w;
}