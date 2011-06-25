package com.gregmcnew.android.pax;

public class Laser extends Projectile {

	public static final int DAMAGE = 10;
	public static final float DIAMETER = 3;
	public static final float INITIAL_VELOCITY = 1000;
	public static final float TURN_SPEED = 0;
	public static final float[] ACCELERATIONLIMS = {0, 0};
	public static final float MAX_VELOCITY = 1000;
	public static final int MAX_LIFE_MS = 1000;
	
	public static final int LENGTH = 20;
	
	public static final float[] EXTRA_POINT_OFFSETS = { LENGTH / 2, LENGTH / 4, -LENGTH / 4, -LENGTH / 2 };
	public static final int NUM_EXTRA_POINTS = EXTRA_POINT_OFFSETS.length;

	protected Laser(Ship parent) {
		super(Entity.LASER, null, null, MAX_LIFE_MS, DAMAGE, DIAMETER, LENGTH, TURN_SPEED, ACCELERATIONLIMS, MAX_VELOCITY);
		body.center.set(parent.body.center);
		heading = parent.heading;
		targetHeading = heading;
		
		setExtraPoints(NUM_EXTRA_POINTS, EXTRA_POINT_OFFSETS);
		
		velocity.x = (float) Math.cos(heading) * INITIAL_VELOCITY;
		velocity.y = (float) Math.sin(heading) * INITIAL_VELOCITY;
	}
	
	@Override
	public void updateHeading(long dt) {
	}
	
	@Override
	public void updateVelocity(long dt) {
	}
}
