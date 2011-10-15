package com.gregmcnew.android.pax;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

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
	
	public void draw(GL10 gl, Painter starPainter, float width, float height) {
		float scale = Math.max(width, height);
		float size = 5f;
		for (Star star : mStars) {
			float alpha = star.mAge < 1000 ? ((float) star.mAge / 1000) : 1f;
			starPainter.draw(gl, star.mX * scale, star.mY * scale, size, size, 0f, alpha);
		}
	}
	
	private Star mStars[];
	private Random mRandom;
	
	private class Star {
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
