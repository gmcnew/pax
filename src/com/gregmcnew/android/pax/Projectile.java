package com.gregmcnew.android.pax;

public class Projectile extends Entity {

	public int lifeMs;
	
	protected Projectile(int id, Type type, int LifeMs, int health, float diameter, float turnSpeed, float acceleration) {
		super(id, type, health, diameter, turnSpeed, acceleration);
		lifeMs = LifeMs;
	}
}
