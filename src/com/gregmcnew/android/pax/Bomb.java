package com.gregmcnew.android.pax;

public class Bomb extends Projectile {

	public static final int DAMAGE = 200;
	public static final float DIAMETER = 8;
	public static final float INITIAL_VELOCITY = 313;
	public static final float TURN_SPEED = 0;
	public static final float[] ACCELERATIONLIMS = {0, 0};
	public static final float MAXVELOCITY = 500;
	public static final int MAX_LIFE_MS = 1000;

	protected Bomb(Ship parent) {
		super(Entity.BOMB, null, null, MAX_LIFE_MS, DAMAGE, DIAMETER, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXVELOCITY);
		reset(parent);
	}
	
	@Override
	public void reset(Ship parent) {
		super.reset(parent);
		
		// Parent may be null if this projectile is being preallocated (i.e.,
		// created and instantly recycled).
		if (parent != null) {
			body.center.set(parent.body.center);
			velocity.set(parent.velocity);
			heading = parent.targetHeading;
		}
		
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		velocity.add(headingX * INITIAL_VELOCITY, headingY * INITIAL_VELOCITY);
	}
}
