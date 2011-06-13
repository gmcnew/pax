package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {

	protected Ship(int id, Type type, int health, float diameter, float turnSpeed, float acceleration, float maxSpeed) {
		super(id, type, health, diameter, turnSpeed, acceleration, maxSpeed);
	}
	
	public abstract Type[] getTargetPriorities();
	
	public abstract boolean canShoot();

	public abstract void updateHeading();
}
