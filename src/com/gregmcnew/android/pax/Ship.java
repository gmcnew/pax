package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {
	
	@SuppressWarnings("hiding")
	public static int[] TYPES = { Entity.FIGHTER, Entity.BOMBER, Entity.FRIGATE, Entity.FACTORY };
	
	protected Ship(int type, int[] targetPriorities, float[] targetSearchLimits, int health, float diameter, float turnSpeed, float[] accelerationLimits, float maxVelocity) {
		super(type, targetPriorities, targetSearchLimits, health, diameter, turnSpeed, accelerationLimits, maxVelocity);
	}
		
	public abstract boolean canShoot();
}
