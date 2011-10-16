package com.gregmcnew.android.pax;

import java.util.Random;

public class StarField {
	
	public static final int NUM_STARS = 100;
	
	public StarField() {
		mRandom = new Random();
		mStars = new Star[NUM_STARS];
		for (int i = 0; i < NUM_STARS; i++) {
			mStars[i] = new Star();
		}
	}
		
	public void update(long dt) {
		float movement = (((float) dt) / 20000) * Pax.sGameSpeed;
		
		for (Star star : mStars) {
			star.mAge += dt;
			star.mX *= 1f + movement;
			star.mY *= 1f + movement;
			if (star.mX < -0.5f || 0.5f < star.mX || star.mY < -0.5f || 0.5f < star.mY) {
				star.reset();
			}
		}
	}
	
	public Star mStars[];
	private Random mRandom;
	
	public class Star {
		public Star() {
			reset();
		}
		
		public void reset() {
			mX = mRandom.nextFloat() - 0.5f;
			mY = mRandom.nextFloat() - 0.5f;
			mAge = 0;
		}
		
		public float mX;
		public float mY;
		public float mAge;
	}
}
