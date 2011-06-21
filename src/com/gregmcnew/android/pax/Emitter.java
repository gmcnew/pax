package com.gregmcnew.android.pax;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Emitter {
	
	public static final int SMOKE = 0;
	public static final int SPARK = 1;
	public static final int[] TYPES = { SMOKE, SPARK };
	
	public static final long[] AGES = { 500, 100 };
	
	public Emitter(int type) {
		mType = type;
		mAgeMs = AGES[type];
		mTime = 0;
		mParticles = new ArrayBlockingQueue<Particle>(1024);
	}
	
	public void update(long dt) {
		mTime += dt;
		while (!mParticles.isEmpty() && mParticles.peek().timeLeft < 0) {
			mParticles.remove();
		}
		
		float dtS = (float) dt / 1000;
		for (Particle p : mParticles) {
			p.x += p.velX * dtS;
			p.y += p.velY * dtS;
			p.timeLeft -= dt;
		}
	}
	
	public void add(float x, float y, float velX, float velY) {
		mParticles.add(new Particle(mAgeMs, x, y, velX, velY));
	}
	
	public class Particle {
		public Particle(long TimeLeft, float X, float Y, float VelX, float VelY) {
			timeLeft = TimeLeft;
			x = X;
			y = Y;
			velX = VelX;
			velY = VelY;
		}
		
		public float x;
		public float y;
		public long timeLeft;
		public final float velX;
		public final float velY;
	}

	public final int mType;
	public final Queue<Particle> mParticles;
	
	public final long mAgeMs;
	
	private int mTime;
}
