package com.gregmcnew.android.pax;

import android.graphics.PointF;

public abstract class Entity {
	public final int id;
	public final Type type;
	public final float radius;
	public final float diameter;
	public final float turnSpeed;
	public final float acceleration;
	public final boolean isShip;
	
	public int health;
	public float heading;
	
	public PointF location;
	//public PointF targetLocation;
	public PointF velocity;
	
	public static enum Type { FIGHTER, BOMBER, FRIGATE, FACTORY, LASER, BOMB, MISSILE };
	
	protected Entity(int Id, Type Type, int Health, float Diameter, float TurnSpeed, float Acceleration) {
		id = Id;
		type = Type;
		diameter = Diameter;
		radius = diameter / 2;
		turnSpeed = TurnSpeed;
		acceleration = Acceleration;
		
		isShip = (Entity.Type.FIGHTER.ordinal() <= type.ordinal() && type.ordinal() <= Entity.Type.FACTORY.ordinal());
		
		health = Health;
		heading = (float) Math.random() * 360;
		
		location = new PointF();
		velocity = new PointF();
	}
	
	public abstract boolean canShoot();
	
	public abstract Type[] getTargetPriorities();
}
