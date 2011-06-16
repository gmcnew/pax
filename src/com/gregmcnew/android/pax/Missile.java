package com.gregmcnew.android.pax;

public class Missile extends Projectile {

	public static final int HEALTH = 40;
	public static final float DIAMETER = 3;
	public static final float INITIAL_VELOCITY = 4;
	public static final float TURN_SPEED = 0.035f;
	public static final float[] ACCELERATIONLIMS = {175f, 87.5f};
	public static final float MAXVELOCITY = 4f;
	
	// TODO: Set missile target priorities and search limits.
	public static final int[] TARGET_PRIORITIES = { };
	public static final float[] TARGET_SEARCH_LIMITS = { };
	
	public static final int MAX_LIFE_MS = 5000;

	protected Missile(Ship parent) {
		super(Entity.LASER, TARGET_PRIORITIES, TARGET_SEARCH_LIMITS, MAX_LIFE_MS, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXVELOCITY);
		body.center.set(parent.body.center);
		velocity.set(parent.velocity);
		heading = parent.heading;
		
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		velocity.offset(headingX * INITIAL_VELOCITY, headingY * INITIAL_VELOCITY);
	}
}
