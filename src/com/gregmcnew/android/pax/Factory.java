package com.gregmcnew.android.pax;

public class Factory extends Ship {
	
	public static final int HEALTH = 20000;
	public static final float DIAMETER = 135;
	public static final float TURN_SPEED = 0.03125f; // in radians per second
	public static final float[] ACCELERATIONLIMS = {3f, 1.5f};
	public static final float MAXSPEED = 6f;

	protected Factory() {
		super(Entity.FACTORY, null, null, HEALTH, DIAMETER, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);
	}
	
	@Override
	public void updateVelocity(long dt){
		velocity.x = maxSpeed * (float)Math.cos(heading);
		velocity.y = maxSpeed * (float)Math.sin(heading);
	}
	
	@Override
	public void updateHeading(long dt) {
		heading -= TURN_SPEED * dt / 1000;
	}
}
