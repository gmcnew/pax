package com.gregmcnew.android.pax;

public class Laser extends Projectile {

	public static final int HEALTH = 40;
	public static final float DIAMETER = 3;
	public static final float INITIAL_VELOCITY = 1000;
	public static final float TURN_SPEED = 0;
	public static final float[] ACCELERATIONLIMS = {0, 0};
	public static final float MAXVELOCITY = 1000;
	public static final int MAX_LIFE_MS = 1000;

	protected Laser(Ship parent) {
		super(Entity.LASER, null, null, MAX_LIFE_MS, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXVELOCITY);
		body.center.set(parent.body.center);
		heading = parent.heading;
		targetHeading = heading;
		
		velocity.x = (float) Math.cos(heading) * INITIAL_VELOCITY;
		velocity.y = (float) Math.sin(heading) * INITIAL_VELOCITY;
	}
	
	@Override
	public void updateHeading(){
	}
	
	@Override
	public void updateVelocity(){
	}
}
