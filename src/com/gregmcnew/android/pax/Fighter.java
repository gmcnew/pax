package com.gregmcnew.android.pax;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Fighter extends Ship {
	
	public static final int HEALTH = 40;
	public static final float DIAMETER = 7.5f;
	public static final float TURN_SPEED = 1.875f; // in radians per second
	public static final float[]ACCELERATIONLIMS = {120, 60};
	public static final float MAXSPEED = 150;
	public static final int[] TARGET_PRIORITIES = { Entity.BOMBER, Entity.FIGHTER, Entity.FRIGATE, Entity.FACTORY };
	public static final int SHOT_INTERVAL = 6;
	
	private static final long TRAIL_LENGTH_MS = 250;
	private static final long TRAIL_POINT_INTERVAL = 50;
	
	public static final float MIN_PREFERRED_TARGET_DISTANCE = 250;
	
	protected Fighter() {
		super(Entity.FIGHTER, TARGET_PRIORITIES, null, HEALTH, DIAMETER, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);
		reset(null);
	}
	
	@Override
	public void reset(Ship parent) {
		super.reset(parent);
		reloadTimeMs = 6000;
		shotTimeMs = 33;
		clipSize = 5;
		shotsLeft = clipSize;
		
		// Run away by default. This avoids scenarios in which enemy ships are
		// hovering over our factory and newly-spawned fighters turn in circles
		// uselessly instead of moving far enough away to get a decent shot.
		mIsRunningAway = true;
	}
	
	protected boolean mIsRunningAway;
	
}
