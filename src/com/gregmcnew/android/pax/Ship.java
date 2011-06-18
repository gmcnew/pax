package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {
	
	public int shotsLeft = 0;
	public int updatesSinceShot = Integer.MAX_VALUE;
	public int shotInterval = 0; // Says how many intervals must pass before shooting again while burst shooting.
	
	public static final int[] TYPES = { Entity.FIGHTER, Entity.BOMBER, Entity.FRIGATE, Entity.FACTORY };
	
	protected Ship(int type, int[] targetPriorities, float[] targetSearchLimits, int health, float diameter, float turnSpeed, float[] accelerationLimits, float maxVelocity) {
		super(type, targetPriorities, targetSearchLimits, health, diameter, turnSpeed, accelerationLimits, maxVelocity);
	}
		
	public abstract boolean canShoot();
}
