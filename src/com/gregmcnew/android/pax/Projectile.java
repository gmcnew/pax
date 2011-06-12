package com.gregmcnew.android.pax;

public class Projectile extends Entity {

	public int lifeMs;
	
	protected Projectile(int id, Type type, int LifeMs, int health, float diameter, float turnSpeed, float acceleration, float maxVelocity) {
		super(id, type, health, diameter, turnSpeed, acceleration, maxVelocity);
		lifeMs = LifeMs;
	}
}
