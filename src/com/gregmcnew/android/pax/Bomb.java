package com.gregmcnew.android.pax;

public class Bomb extends Projectile {

	public static final int HEALTH = 40;
	public static final float DIAMETER = 3;
	public static final float INITIAL_VELOCITY = 500;
	public static final float TURN_SPEED = 0;
	public static final float[] ACCELERATIONLIMS = {-15, 0};
	public static final float MAXVELOCITY = 500;
	public static final int MAX_LIFE_MS = 1000;

	protected Bomb(Ship parent) {
		super(Entity.BOMB, null, null, MAX_LIFE_MS, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXVELOCITY);
		body.center.set(parent.body.center);
		velocity.set(parent.velocity);
		heading = parent.targetHeading;
		
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		velocity.offset(headingX * INITIAL_VELOCITY, headingY * INITIAL_VELOCITY);
	}
	
	@Override
	public void updateHeading(){
	}
	
	@Override
	public void updateVelocity(){
		float cosH = (float)Math.cos(heading);
		float sinH = (float)Math.sin(heading);
		float velH = velocity.y * sinH + velocity.x * cosH; //Speed in the direction of heading.
		velH += ACCELERATIONLIMS[0];
		velocity.x = velH * cosH;
		velocity.y = velH * sinH;
	}
}
