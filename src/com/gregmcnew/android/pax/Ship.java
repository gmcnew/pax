package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {
	
	public long reloadTimeMs;
	public long reloadTimer;
	public long shotTimeMs;
	public long shotTimer;
	public int clipSize;
	public int shotsLeft;
	
	@SuppressWarnings("hiding")
	public static final int[] TYPES = { Entity.FIGHTER, Entity.BOMBER, Entity.FRIGATE, Entity.FACTORY };
	
	public static final int[] TYPES_LARGEST_FIRST = { Entity.FACTORY, Entity.FRIGATE, Entity.BOMBER, Entity.FIGHTER };
	
	protected Ship(int type, int[] targetPriorities, float[] targetSearchLimits, int health, float diameter, float length, float turnSpeed, float[] accelerationLimits, float maxVelocity) {
		super(type, targetPriorities, targetSearchLimits, health, diameter, length, turnSpeed, accelerationLimits, maxVelocity);
	}
	
	@Override
	public void reset(Ship parent) {
		super.reset(parent);

		reloadTimeMs = 0;
		reloadTimer = 0;
		
		shotTimeMs = 0;
		shotTimer = 0;
		
		clipSize = 0;
		shotsLeft = 0;
	}
	
	public boolean shoot(long dt) {
		boolean shoot = false;
		
		reloadTimer += dt;
		shotTimer += dt;
		if (reloadTimer > reloadTimeMs) {
			reloadTimer -= reloadTimeMs;
			shotsLeft = clipSize;
		}
		
		if (target != null && shotsLeft > 0 && shotTimer > shotTimeMs) {
			// Don't let a fighter shoot unless it's looking almost directly at
			// its target point (and isn't running away).
			if (type == Entity.FIGHTER) {
				Fighter fighter = (Fighter) this;
				
				float absHeadingToTargetHeading = (headingToTargetHeading < 0)
					? -headingToTargetHeading
					: headingToTargetHeading;
				
				if (!fighter.mIsRunningAway && absHeadingToTargetHeading < 0.1f) {
					shoot = true;
				}
			}
			else {
				shoot = true;
			}
			
			if (shoot) {
				// Start the reload timer as soon as the clip becomes partially
				// empty.
				if (shotsLeft == clipSize) {
					reloadTimer = 0;
				}
				shotsLeft--;
				
				if (shotTimeMs == 0) {
					shotTimer = 0;
				}
				else {
					shotTimer %= shotTimeMs;
				}
			}
		}
		
		return shoot;
	}
}
