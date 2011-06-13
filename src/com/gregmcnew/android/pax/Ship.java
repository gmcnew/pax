package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {

	protected Ship(int id, Type type, Type[] targetPriorities, int health, float diameter, float turnSpeed, float acceleration, float maxVelocity) {
		super(id, type, targetPriorities, health, diameter, turnSpeed, acceleration, maxVelocity);
	}
	
	public abstract boolean canShoot();

	public abstract void updateCourse();
}
