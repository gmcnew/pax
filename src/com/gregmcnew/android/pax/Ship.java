package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {

	protected Ship(Type type, Type[] targetPriorities, int health, float diameter, float turnSpeed, float acceleration, float maxVelocity) {
		super(type, targetPriorities, health, diameter, turnSpeed, acceleration, maxVelocity);
	}
		
	public abstract boolean canShoot();

	public abstract void updateHeading();
}
