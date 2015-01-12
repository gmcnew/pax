package com.gregmcnew.android.pax;

public class AIWeights {
	
	// These weights were determined through experimentation (see pax/aitest).
	// The final weight [0..1] indicates the importance of enemy health. 0 means
	// a nearly-dead enemy is treated as equal to a full-health enemy. 1 means a
	// nearly-dead enemy is almost ignored.
	private static final float OPTIMAL_WEIGHTS[] = {
			0.1466f, // fighter
			1.01f,   // bomber
			1.0f,	 // frigate
			0.7845f  // enemy health
			};

	public static final int NUM_WEIGHTS = OPTIMAL_WEIGHTS.length;
	
	public AIWeights() {
		w = new float[NUM_WEIGHTS];
		reset();
	}
	
	public static final int FIGHTER_X    = 0;
	public static final int BOMBER_X     = 1;
	public static final int FRIGATE_X    = 2;
	public static final int ENEMY_HEALTH = 3;
	
	public void reset() {
		for (int i = 0; i < NUM_WEIGHTS; i++) {
			w[i] = OPTIMAL_WEIGHTS[i];
		}
	}
	
	public void randomize() {
		for (int i = 0; i < NUM_WEIGHTS; i++) {
			w[i] = Game.sRandom.nextFloat();
		}
		if (w[FRIGATE_X] != 0) {
			w[FIGHTER_X] /= w[FRIGATE_X];
			w[BOMBER_X] /= w[FRIGATE_X];
			w[FRIGATE_X] = 1;
		}
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