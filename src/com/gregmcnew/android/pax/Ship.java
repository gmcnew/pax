package com.gregmcnew.android.pax;

import android.graphics.PointF;

public abstract class Ship {
	public final int id;
	public final ShipType type;
	public final float radius;
	public final float diameter;
	public final float turnSpeed;
	public final float acceleration;
	
	public int health;
	public float heading;
	
	public PointF location;
	//public PointF targetLocation;
	public PointF velocity;
	
	public static enum ShipType { FIGHTER, BOMBER, FRIGATE, FACTORY };
	
	protected Ship(int Id, ShipType Type, int Health, float Diameter, float TurnSpeed, float Acceleration) {
		id = Id;
		type = Type;
		diameter = Diameter;
		radius = diameter / 2;
		turnSpeed = TurnSpeed;
		acceleration = Acceleration;
		
		health = Health;
		heading = (float) Math.random() * 360;
		
		location = new PointF();
		velocity = new PointF();
	}
	
	public abstract ShipType[] getTargetPriorities();
}
