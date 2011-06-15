package com.gregmcnew.android.pax;

import android.graphics.PointF;

/**Abstract class. Represents any entity in the game's id, health, velocity, etc.**/
public abstract class Entity {
	
	public static int NO_ENTITY = -1;
	public final Type type;
	public final Type[] targetPriorities;
	public final float[] targetSearchLimits;
	public final float radius;
	public final float diameter;
	public final float turnSpeed;
	public final float[] accelerationLimits;
	public final float maxSpeed;
	public final float pi = (float)Math.PI;
	
	public int health;
	
	public Entity target;
	public float targetHeading = pi/2; 
	public float difference; // difference between heading and targetHeading.	
	public CircleF body;
	public float heading; // in radians
	public PointF velocity;
	
	protected int id;

	public static enum Type { FIGHTER, BOMBER, FRIGATE, FACTORY, LASER, BOMB, MISSILE };
	
	protected Entity(Type Type, Type[] TargetPriorities, float[] TargetSearchLimits, int Health, float Diameter, float TurnSpeed, float[] AccelerationLimits, float MaxSpeed) {
		type = Type;
		targetPriorities = TargetPriorities;
		targetSearchLimits = TargetSearchLimits;
		radius = Diameter / 2;
		diameter = Diameter;
		turnSpeed = TurnSpeed;
		accelerationLimits = AccelerationLimits;
		maxSpeed = MaxSpeed;
		
		health = Health;
		
		target = null;
		
		id = NO_ENTITY;
		
		body = new CircleF(new PointF(), radius);
		heading = (float) (Math.random() * Math.PI * 2);
		velocity = new PointF();
	}
	
	public float getSpeed(){
		float v_x = velocity.x;
		float v_y = velocity.y;
		return (float) Math.sqrt(v_x*v_x + v_y*v_y);
	}
	
	/**Sets the ship to max speed on its current heading.**/
	public void fullSpeedAhead(){
		velocity.x = (float) Math.cos(heading) * maxSpeed;
		velocity.y = (float) Math.sin(heading) * maxSpeed;
	}
	
	public boolean wantsNewTarget() {
		// TODO: Be smarter about whether an entity wants a new target.
		return targetPriorities != null && Math.random() > 0.99f;
	}
	
	public void updatePosition(){
		float dx_t = velocity.x * Pax.UPDATE_INTERVAL_MS / 1000;
		float dy_t = velocity.y * Pax.UPDATE_INTERVAL_MS / 1000;
		body.center.offset(dx_t, dy_t);
	}
	
	public void updateHeading(){
		//Gets the difference within +/- 2*pi.
		difference = (targetHeading - heading) % (pi*2);
		
		//Gets difference within +/- pi.
		if(Math.abs(difference) >= pi){
			difference += (difference > 0) ? -2*pi : 2*pi;
		}
		
		if(Math.abs(difference) >= turnSpeed){ //If the difference is less than what the ship can turn in one update...
			if(difference >= 0){
				heading += turnSpeed;
			} else {
				heading -= turnSpeed;
			}
		} else {
			heading += difference;
		}
	}
	
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
		if(Math.abs(difference) > pi/2){
			velH -= accelerationLimits[0];
			if(-velH > maxSpeed) velH = -maxSpeed;
		} else { 
			velH += accelerationLimits[0]*Math.cos(difference);
			if(velH > maxSpeed) velH = maxSpeed;
		}
		velocity.x = velH * cosH + velP * sinH;
		velocity.y = velH * sinH - velP * cosH;
	}
	
	public void move(){
		updatePosition();
		updateHeading();
		updateVelocity();
	}
}
