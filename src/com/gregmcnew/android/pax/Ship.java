package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {

	protected Ship(int id, Type type, int health, float diameter, float turnSpeed, float acceleration) {
		super(id, type, health, diameter, turnSpeed, acceleration);
	}
	
	public abstract Type[] getTargetPriorities();
	
	public abstract boolean canShoot();
}
