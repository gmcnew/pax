package com.gregmcnew.android.pax;

public class Projectile extends Entity {

	public int lifeMs;
	
	protected Projectile(int id, Type type, Type[] targetPriorities, int LifeMs, int health, float diameter, float turnSpeed, float acceleration, float maxVelocity) {
		super(id, type, targetPriorities, health, diameter, turnSpeed, acceleration, maxVelocity);
		lifeMs = LifeMs;
	}
}
