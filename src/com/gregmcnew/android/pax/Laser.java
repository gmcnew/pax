package com.gregmcnew.android.pax;

public class Laser extends Projectile {

	public static final int HEALTH = 40;
	public static final float DIAMETER = 3;
	public static final float INITIAL_VELOCITY = 3;
	public static final float TURN_SPEED = 0;
	public static final float[] ACCELERATIONLIMS = {0f, 0f};
	public static final float MAXVELOCITY = 3f;
	public static final int MAX_LIFE_MS = 1000;

	protected Laser(Ship parent) {
		super(Entity.LASER, null, null, MAX_LIFE_MS, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXVELOCITY);
		body.center.set(parent.body.center);
		velocity.set(parent.velocity);
		heading = parent.heading;
		
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		velocity.offset(headingX * INITIAL_VELOCITY, headingY * INITIAL_VELOCITY);
	}
}
