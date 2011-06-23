package com.gregmcnew.android.pax;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Emitter {
	
	public static final int SMOKE = 0;
	public static final int SPARK = 1;
	public static final int LASER_HIT = 2;
	public static final int MISSILE_HIT = 3;
	public static final int BOMB_HIT = 4;
	public static final int SHIP_EXPLOSION = 5;
	
	public static final int[] TYPES = { SMOKE, SPARK, LASER_HIT, MISSILE_HIT, BOMB_HIT, SHIP_EXPLOSION };
	public static final long[] AGES = { 500, 100, 100, 100, 500, 1250 };
	
	
	private static final int NO_THROTTLE = -1;
	
	
	public final int mType;
	public final Queue<Particle> mParticles;
	public final long mInitialLifeMs;
	
	
	// Shorten particle lifespans as framerates drop. Throttling begins at
	// mThrottleStartFps, and the lifespans of particles decrease gradually
	// until at mThrottleMinFps (or lower) no particles appear at all.
	
	// Also, when the framerate is below mIgnoreAddFps, add() will refuse to add
	// new particles.
	
	private int mThrottleStartFps;
	private int mThrottleMinFps;
	private int mIgnoreAddFps;
	
	public Emitter(int type) {
		mType = type;
		mInitialLifeMs = AGES[type];
		mParticles = new ArrayBlockingQueue<Particle>(1024);
		
		if (Pax.SELF_BENCHMARK || type == SHIP_EXPLOSION) {
			mThrottleStartFps = NO_THROTTLE;
			mThrottleMinFps = NO_THROTTLE;
			mIgnoreAddFps = NO_THROTTLE;
		}
		else {
			mThrottleStartFps = 40;
			mThrottleMinFps = 25;
			mIgnoreAddFps = (int) (mThrottleStartFps + mThrottleMinFps) / 2;
		}
	}
	
	public void update(long dt) {
		int fps = FramerateCounter.getFPS();
		
		// By default, remove particles when they're dead.
		long thresholdOfDeath = 0;
		
		if (mThrottleMinFps != NO_THROTTLE && fps < mThrottleMinFps) {
			// Remove particles as soon as they're spawned.
			thresholdOfDeath = mInitialLifeMs;
		}
		else if (mThrottleStartFps != NO_THROTTLE && fps < mThrottleStartFps) {
			// Set thresholdOfDeath to a value from [0 .. mInitialLifeMs) based
			// on where we are in the throttling window.
			int throttleWindow = mThrottleStartFps - mThrottleMinFps;
			thresholdOfDeath = mInitialLifeMs * (fps - mThrottleMinFps) / throttleWindow;
		}
		
		while (!mParticles.isEmpty() && mParticles.peek().life <= thresholdOfDeath) {
			mParticles.remove();
		}
		
		float dtS = (float) dt / 1000;
		for (Particle p : mParticles) {
			p.x += p.velX * dtS;
			p.y += p.velY * dtS;
			p.life -= dt;
		}
	}
	
	public void add(float scale, float x, float y, float velX, float velY) {
		// Only add new particles if we're above the midpoint of the
		// throttling window.
		if (mIgnoreAddFps == NO_THROTTLE || FramerateCounter.getFPS() > mIgnoreAddFps) {
			mParticles.add(new Particle(mInitialLifeMs, scale, x, y, velX, velY));
		}
	}
	
	public class Particle {
		public Particle(long Life, float Scale, float X, float Y, float VelX, float VelY) {
			life = Life;
			scale = Scale;
			x = X;
			y = Y;
			velX = VelX;
			velY = VelY;
		}
		
		public float x;
		public float y;
		public long life; // in milliseconds
		public final float velX;
		public final float velY;
		public final float scale;
	}
}
