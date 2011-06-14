package com.gregmcnew.android.pax;

public class Missile extends Projectile {

	public static int HEALTH = 40;
	public static float DIAMETER = 3;
	public static float INITIAL_VELOCITY = 4;
	public static float TURN_SPEED = 0.035f;
	public static float ACCELERATION = 0;
	public static float MAXVELOCITY = 4f;
	
	// TODO: Set missile target priorities and search limits.
	public static Type[] TARGET_PRIORITIES = { };
	public static float[] TARGET_SEARCH_LIMITS = { };
	
	public static int MAX_LIFE_MS = 5000;

	protected Missile(Ship parent) {
		super(Type.LASER, TARGET_PRIORITIES, TARGET_SEARCH_LIMITS, MAX_LIFE_MS, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION, MAXVELOCITY);
		body.center.set(parent.body.center);
		velocity.set(parent.velocity);
		heading = parent.heading;
		
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		velocity.offset(headingX * INITIAL_VELOCITY, headingY * INITIAL_VELOCITY);
	}
}
