package com.gregmcnew.android.pax;

public class Laser extends Projectile {

	public static final int DAMAGE = 10;
	public static final float DIAMETER = 3;
	public static final float TURN_SPEED = 0;
	public static final float[] ACCELERATIONLIMS = {0, 0};
	public static final float MAX_VELOCITY = 1000;
	public static final int MAX_LIFE_MS = 1000 * 1000 / (int)MAX_VELOCITY;
	
	public static final int LENGTH = 20;
	
	public static final float[] EXTRA_POINT_OFFSETS = { LENGTH / 2, LENGTH / 4, -LENGTH / 4, -LENGTH / 2 };
	public static final int NUM_EXTRA_POINTS = EXTRA_POINT_OFFSETS.length;

	public Laser(Ship parent) {
		super(Entity.LASER, null, null, MAX_LIFE_MS, DAMAGE, DIAMETER, LENGTH, TURN_SPEED, ACCELERATIONLIMS, MAX_VELOCITY);
		
		mExtraPoints = new Point2[NUM_EXTRA_POINTS];
		for (int i = 0; i < NUM_EXTRA_POINTS; i++) {
			mExtraPoints[i] = new Point2();
		}
		
		reset(parent);
	}
	
	@Override
	public void reset(Ship parent) {
		super.reset(parent);
		
		// Parent may be null if this projectile is being preallocated (i.e.,
		// created and instantly recycled).
		if (parent != null) {
			body.center.set(parent.body.center);
			heading = parent.heading;
		}
		targetHeading = heading;
		
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		
		body.center.add(headingX * length / 2, headingY * length / 2);
		
		setExtraPoints(NUM_EXTRA_POINTS, EXTRA_POINT_OFFSETS);
		
		velocity.x = headingX * MAX_VELOCITY;
		velocity.y = headingY * MAX_VELOCITY;
	}
	
	@Override
	public void updateHeading(long dt) {
	}
	
	@Override
	public void updateVelocity(long dt) {
	}
}
