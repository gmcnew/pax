package com.gregmcnew.android.pax;

public class Fighter extends Ship {
	
	public static final int HEALTH = 40;
	public static final float DIAMETER = 7.5f;
	public static final float TURN_SPEED = 0.025f; //in radians per 40ms
	public static final float[]ACCELERATIONLIMS = {120, 60};
	public static final float MAXSPEED = 150;
	public static final int[] TARGET_PRIORITIES = { Entity.BOMBER, Entity.FIGHTER, Entity.FRIGATE, Entity.FACTORY };
	public static final int SHOT_INTERVAL = 6;
	

	protected Fighter() {
		super(Entity.FIGHTER, TARGET_PRIORITIES, null, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);
		shotInterval = SHOT_INTERVAL;
	}

	@Override
	public boolean canShoot() {
		if(shotsLeft > 0) return false;
		mShootCounter = (mShootCounter + 1) % 20;
		return mShootCounter == 0;
	}
	
	private int mShootCounter = 0;
}
