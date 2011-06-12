package com.gregmcnew.android.pax;

import android.graphics.PointF;

public abstract class Entity {
	public final int id;
	public final Type type;
	public final float radius;
	public final float diameter;
	public final float turnSpeed;
	public final float acceleration;
	
	public int health;
	
	public CircleF body;
	public float heading;
	public PointF velocity;
	
	public static enum Type { FIGHTER, BOMBER, FRIGATE, FACTORY, LASER, BOMB, MISSILE };
	
	protected Entity(int Id, Type Type, int Health, float Diameter, float TurnSpeed, float Acceleration) {
		id = Id;
		type = Type;
		radius = Diameter / 2;
		diameter = Diameter;
		turnSpeed = TurnSpeed;
		acceleration = Acceleration;
		
		health = Health;
		
		body = new CircleF(new PointF(), radius);
		heading = (float) Math.random() * 360;
		velocity = new PointF();
	}
}
