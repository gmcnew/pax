package com.gregmcnew.android.pax;

public class Missile extends Projectile {

	public static final int HEALTH = 40;
	public static final float DIAMETER = 3;
	public static final float INITIAL_VELOCITY = 100;
	public static final float TURN_SPEED = 0.1f;
	public static final float[] ACCELERATIONLIMS = {100f, 35f};
	public static final float MAXVELOCITY = 400;
	
	// TODO: Set missile target priorities and search limits.
	public static final int[] TARGET_PRIORITIES = {Entity.FIGHTER, Entity.BOMBER, Entity.FRIGATE, Entity.FACTORY };
	public static final float[] TARGET_SEARCH_LIMITS = {500, 500, 500, 500 };
	
	public static final int MAX_LIFE_MS = 5000;

	protected Missile(Ship parent) {
		super(Entity.LASER, TARGET_PRIORITIES, TARGET_SEARCH_LIMITS, MAX_LIFE_MS, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXVELOCITY);
		body.center.set(parent.body.center);
		heading = parent.heading + pi + (float)(Math.random() - .5) * pi / 4;
		float headingX = (float) Math.cos(heading);
		float headingY = (float) Math.sin(heading);
		velocity.set(headingX * INITIAL_VELOCITY, headingY * INITIAL_VELOCITY);
		targetHeading = parent.targetHeading;
		target = parent.target;
	}
	
	@Override
	public void updateVelocity(){
		//Find velocities components in terms of the ships coord. frame.
		float cosH = (float)Math.cos(heading);
		float sinH = (float)Math.sin(heading);
		float velH = velocity.y * sinH + velocity.x * cosH; //Speed in the direction of heading.
		float velP = velocity.x * sinH - velocity.y * cosH; //Speed in the direction 90deg CW of heading.
		if(velP > 0){
			velP = (velP > accelerationLimits[1]) ? velP - accelerationLimits[1] : 0;
		} else if(velP < 0) {
			velP = (Math.abs(velP) > accelerationLimits[1]) ? velP + accelerationLimits[1] : 0;
		}
		if(Math.abs(difference) > pi/4){
			velH -= accelerationLimits[0];
			if(velH < .75*maxSpeed) velH = (float).75*maxSpeed;
		} else { 
			velH += accelerationLimits[0]*Math.cos(difference);
			if(velH > maxSpeed) velH = maxSpeed;
		}
		velocity.x = velH * cosH + velP * sinH;
		velocity.y = velH * sinH - velP * cosH;
		
	}
}
