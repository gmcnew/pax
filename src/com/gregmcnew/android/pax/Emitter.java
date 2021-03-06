package com.gregmcnew.android.pax;

public class Emitter {
	
	public static final int SMOKE = 0;
	public static final int SPARK = 1;
	public static final int LASER_HIT = 2;
	public static final int MISSILE_HIT = 3;
	public static final int BOMB_HIT = 4;
	public static final int SHIP_EXPLOSION = 5;
	public static final int UPGRADE_EFFECT = 6;
	public static final int RED_VICTORY = 7;
	public static final int BLUE_VICTORY = 8;
	public static final int TIE_GAME = 9;
	
	public static final int[] TYPES = { SMOKE, SPARK, LASER_HIT, MISSILE_HIT,
			BOMB_HIT, SHIP_EXPLOSION, UPGRADE_EFFECT,
			RED_VICTORY, BLUE_VICTORY, TIE_GAME };
	public static final long[] AGES = { 500, 100, 100, 100,
			500, 1250, 1000,
			3000, 3000, 3000 };
	
	
	private static final int NO_THROTTLE = -1;
	
	
	public final int mType;
	public final long mInitialLifeMs;
	
	// All particles for a given emitter will be created with the same age. This
	// makes sorting by age easy -- just put new particles at the end. Removing
	// dead particles is easy as well.
	public Particle[] mParticles;
	public int mStart;
	public int mEnd;
	public int mCapacity;
	
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
		
		mCapacity = 512;
		mParticles = new Particle[mCapacity];
		mStart = 0;
		mEnd = 0;
		
		if (Constants.SELF_BENCHMARK || type == SHIP_EXPLOSION || type == UPGRADE_EFFECT) {
			mThrottleStartFps = NO_THROTTLE;
			mThrottleMinFps = NO_THROTTLE;
			mIgnoreAddFps = NO_THROTTLE;
		}
		else {
			mThrottleStartFps = 45;
			mThrottleMinFps = 30;
			mIgnoreAddFps = (int) (mThrottleStartFps + mThrottleMinFps) / 2;
		}
	}
	
	public void update(long dt) {
		int fps = (int) FramerateCounter.getFPS();
		
		float elapsedLife = dt;

		if (mType == RED_VICTORY || mType == BLUE_VICTORY || mType == TIE_GAME) {
			// These messages need to remain onscreen regardless of framerate.
		}
		else if (mThrottleMinFps != NO_THROTTLE && fps < mThrottleMinFps) {
			// Remove particles as soon as they're spawned.
			elapsedLife = mInitialLifeMs;
		}
		else if (mThrottleStartFps != NO_THROTTLE && fps < mThrottleStartFps && dt < mInitialLifeMs) {
			// Set elapsedLife to a value from [dt .. mInitialLifeMs) based on
			// where we are in the throttling window.
			int throttleWindow = mThrottleStartFps - mThrottleMinFps;
			
			elapsedLife = dt + (((mInitialLifeMs - dt) * (mThrottleStartFps - fps)) / throttleWindow);
		}
		
		float dtS = (float) dt / 1000;
		for (int i = 0; i < (mEnd - mStart); i++) {
			int offs = (mStart + i) % mCapacity;
			Particle p = mParticles[offs];

			p.velX *= 1f + p.accel * dtS;
			p.velY *= 1f + p.accel * dtS;
			p.x += p.velX * dtS;
			p.y += p.velY * dtS;
			p.life -= elapsedLife;

			if (p.life <= 0) {
				p.recycle();

				mEnd = (mEnd + mCapacity - 1) % mCapacity;
				i--;

				mParticles[offs] = mParticles[mEnd];
				mParticles[mEnd] = null;
			}
		}
	}

	public void addVariable(float scale, float x, float y, float velX, float velY, float life) {
		add(scale,x, y, velX, velY, 0f, life);
	}

	public void add(float scale, float x, float y, float velX, float velY) {
		add(scale, x, y, velX, velY, 0f, 1f);
	}
	
	public void add(float scale, float x, float y, float velX, float velY, float accel, float life) {
		// Only add new particles if we're above the midpoint of the
		// throttling window.
		if (mIgnoreAddFps == NO_THROTTLE || FramerateCounter.getFPS() > mIgnoreAddFps) {
			int newEnd = (mEnd + 1) % mCapacity;
			if (newEnd != mStart) {
				if (mParticles[mEnd] == null) {
					mParticles[mEnd] = Particle.create();
				}
				mParticles[mEnd].reset((long)(mInitialLifeMs * life), scale, x, y, velX, velY, accel);
				mEnd = newEnd;
			}
		}
	}
}
