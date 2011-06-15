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
	public float targetHeading; // in radians
	
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
	
	public void updateHeading(){
		float difference = (heading - targetHeading) % (pi*2);
		
		if(Math.abs(difference) > turnSpeed){
			if(difference > pi){
				heading += turnSpeed;
			} else {
				heading -= turnSpeed;
			}
		} else {
			heading += difference;
		}
	}
	
	public void updatePosition(){
		float dx_t = velocity.x * Pax.UPDATE_INTERVAL_MS / 1000;
		float dy_t = velocity.y * Pax.UPDATE_INTERVAL_MS / 1000;
		body.center.offset(dx_t, dy_t);
	}
	
	public void move(){
		updatePosition();
		updateHeading();
		//Update acceleration
		
		//Update velocity
	}
}
