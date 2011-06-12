package com.gregmcnew.android.pax;

import android.graphics.PointF;

/**Abstract class. Represents any entity in the game's id, health, velocity, etc.**/
public abstract class Entity {
	public final int id;
	public final Type type;
	public final float radius;
	public final float diameter;
	public final float turnSpeed;
	public final float acceleration;
	
	public int health;
	
	public CircleF body;
	public float heading; // in radians
	public PointF velocity;
	public float maxVelocity;
	
	public static enum Type { FIGHTER, BOMBER, FRIGATE, FACTORY, LASER, BOMB, MISSILE };
	
	protected Entity(int Id, Type Type, int Health, float Diameter, float TurnSpeed, float Acceleration, float MaxVel) {
		id = Id;
		type = Type;
		radius = Diameter / 2;
		diameter = Diameter;
		turnSpeed = TurnSpeed;
		acceleration = Acceleration;
		maxVelocity = MaxVel;
		
		health = Health;
		
		body = new CircleF(new PointF(), radius);
		heading = (float) (Math.random() * Math.PI * 2);
		velocity = new PointF();
	}
}
