package com.gregmcnew.android.pax;

public class Missile extends Projectile {

	public static final int HEALTH = 40;
	public static final float DIAMETER = 3;
	public static final float INITIAL_VELOCITY = 100;
	public static final float TURN_SPEED = 0.15f;
	public static final float[] ACCELERATIONLIMS = {100f, 40f};
	public static final float MAXVELOCITY = 400;
	
	// TODO: Set missile target priorities and search limits.
	public static final int[] TARGET_PRIORITIES = {Entity.FIGHTER, Entity.BOMBER, Entity.FRIGATE, Entity.FACTORY };
	public static final float[] TARGET_SEARCH_LIMITS = {500, 500, 500, 500 };
	
	public static final int MAX_LIFE_MS = 5000;

	protected Missile(Ship parent) {
		super(Entity.MISSILE, TARGET_PRIORITIES, TARGET_SEARCH_LIMITS, MAX_LIFE_MS, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXVELOCITY);
		heading = parent.heading + pi + (float)(Math.random() - .5) * pi / 6;
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		body.center.set(parent.body.center);
		body.center.offset(-5 * headingX, -5 * headingY);
		velocity.set(headingX * INITIAL_VELOCITY, headingY * INITIAL_VELOCITY);
		targetHeading = parent.targetHeading;
		target = parent.target;
	}
}
