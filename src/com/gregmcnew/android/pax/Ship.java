package com.gregmcnew.android.pax;

import android.graphics.PointF;

public abstract class Ship {
	public final float size;
	public final float turnSpeed;
	public final float acceleration;
	
	public int health;
	public final int id;
	
	public PointF location;
	//public PointF targetLocation;
	public PointF velocity;
	
	public static enum ShipType { FIGHTER, BOMBER, FRIGATE, FACTORY };
	
	protected Ship(int Id, int Health, float Size, float TurnSpeed, float Acceleration) {
		id = Id;
		health = Health;
		size = Size;
		turnSpeed = TurnSpeed;
		acceleration = Acceleration;
		
		location = new PointF();
		velocity = new PointF();
	}
	
	public abstract ShipType[] getTargetPriorities();
}
