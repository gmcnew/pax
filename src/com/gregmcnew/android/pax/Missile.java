package com.gregmcnew.android.pax;

public class Missile extends Projectile {

	public static final int DAMAGE = 40;
	public static final float DIAMETER = 5;
	public static final float INITIAL_VELOCITY = 100;
	public static final float TURN_SPEED = 3.75f; // in radians per second
	public static final float[] ACCELERATIONLIMS = {100f, 45f};
	public static final float MAX_VELOCITY = 400;
	
	public static final int LENGTH = 10;
	
	public static final float[] EXTRA_POINT_OFFSETS = { LENGTH / 2, -LENGTH / 2 };
	public static final int NUM_EXTRA_POINTS = EXTRA_POINT_OFFSETS.length;
	
	public static final int[] TARGET_PRIORITIES = {Entity.FIGHTER, Entity.BOMBER, Entity.FRIGATE, Entity.FACTORY };
	public static final float[] TARGET_SEARCH_LIMITS = {500, 500, 500, 500 };
	
	public static final int MAX_LIFE_MS = 5000;

	protected Missile(Ship parent) {
		super(Entity.MISSILE, TARGET_PRIORITIES, TARGET_SEARCH_LIMITS, MAX_LIFE_MS, DAMAGE, DIAMETER, LENGTH, TURN_SPEED, ACCELERATIONLIMS, MAX_VELOCITY);
		
		// Pick a side of the ship at random.
		float side = (float) (parent.heading + (Pax.sRandom.nextBoolean() ? Math.PI / 2 : -Math.PI / 2));
		
		heading = (float) (side + (Pax.sRandom.nextFloat() - .5) * Math.PI / 6);
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		body.center.set(parent.body.center);
		body.center.offset(-5 * headingX, -5 * headingY);
		velocity.set(headingX * INITIAL_VELOCITY, headingY * INITIAL_VELOCITY);
		targetHeading = parent.targetHeading;
		target = parent.target;
	}
}
