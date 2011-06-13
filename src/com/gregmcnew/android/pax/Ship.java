package com.gregmcnew.android.pax;

public abstract class Ship extends Entity {
	
	// XXX: Clean this stuff up. Java enums are frustrating.
	public static int FIGHTER = 0;
	public static int BOMBER  = 1;
	public static int FRIGATE = 2;
	public static int FACTORY = 3;
	public static int[] TYPES = { FIGHTER, BOMBER, FRIGATE, FACTORY };
	
	protected Ship(Type type, Type[] targetPriorities, int health, float diameter, float turnSpeed, float acceleration, float maxVelocity) {
		super(type, targetPriorities, health, diameter, turnSpeed, acceleration, maxVelocity);
	}
		
	public abstract boolean canShoot();

	public abstract void updateHeading();
}
