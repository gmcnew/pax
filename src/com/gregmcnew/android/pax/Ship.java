package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {
	
	public static Type[] TYPES = { Entity.Type.FIGHTER, Entity.Type.BOMBER, Entity.Type.FRIGATE, Entity.Type.FACTORY };
	
	protected Ship(Type type, Type[] targetPriorities, int health, float diameter, float turnSpeed, float acceleration, float maxVelocity) {
		super(type, targetPriorities, health, diameter, turnSpeed, acceleration, maxVelocity);
	}
		
	public abstract boolean canShoot();

	public abstract void updateHeading();
}
