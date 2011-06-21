package com.gregmcnew.android.pax;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Emitter {
	
	public static final int SMOKE = 0;
	public static final int SPARK = 1;
	public static final int[] TYPES = { SMOKE, SPARK };
	
	public static final long[] AGES = { 500, 100 };
	
	
	// Shorten particle lifespans as framerates drop. Throttling begins at
	// THROTTLE_START_FPS, and the lifespans of particles decrease gradually
	// until at THROTTLE_MIN_FPS (or lower) no particles appear at all.
	
	// Also, when the framerate is below the midpoint of the throttling window,
	// add() will refuse to add new particles.
	
	private static int THROTTLE_START_FPS = 40;
	private static int THROTTLE_MIN_FPS = 25;
	
	private static int THROTTLE_MIDPOINT_FPS = (THROTTLE_START_FPS + THROTTLE_MIN_FPS) / 2;
	
	
	public Emitter(int type) {
		mType = type;
		mInitialLifeMs = AGES[type];
		mParticles = new ArrayBlockingQueue<Particle>(1024);
	}
	
	public void update(long dt) {
		int fps = FramerateCounter.getFPS();
		
		// By default, remove particles when they're dead.
		long thresholdOfDeath = 0;
		
		if (fps < THROTTLE_MIN_FPS) {
			// Remove particles as soon as they're spawned.
			thresholdOfDeath = mInitialLifeMs;
		}
		else if (fps < THROTTLE_START_FPS) {
			// Set thresholdOfDeath to a value from [0 .. mInitialLifeMs) based
			// on where we are in the throttling window.
			int throttleWindow = THROTTLE_START_FPS - THROTTLE_MIN_FPS;
			thresholdOfDeath = mInitialLifeMs * (fps - THROTTLE_MIN_FPS) / throttleWindow;
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
	
	public void add(float x, float y, float velX, float velY) {
		// Only add new particles if we're above the midpoint of the
		// throttling window.
		if (FramerateCounter.getFPS() > THROTTLE_MIDPOINT_FPS) {
			mParticles.add(new Particle(mInitialLifeMs, x, y, velX, velY));
		}
	}
	
	public class Particle {
		public Particle(long Life, float X, float Y, float VelX, float VelY) {
			life = Life;
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
	}

	public final int mType;
	public final Queue<Particle> mParticles;
	public final long mInitialLifeMs;
}
