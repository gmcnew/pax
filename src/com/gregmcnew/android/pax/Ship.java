package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {
	
	public static Type[] TYPES = { Entity.Type.FIGHTER, Entity.Type.BOMBER, Entity.Type.FRIGATE, Entity.Type.FACTORY };
	
	protected Ship(Type type, Type[] targetPriorities, float[] targetSearchLimits, int health, float diameter, float turnSpeed, float[] accelerationLimits, float maxVelocity) {
		super(type, targetPriorities, targetSearchLimits, health, diameter, turnSpeed, accelerationLimits, maxVelocity);
	}
		
	public abstract boolean canShoot();

	public abstract void updateHeading();
}
