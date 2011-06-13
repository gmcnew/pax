package com.gregmcnew.android.pax;

public class Factory extends Ship {
	
	public static int HEALTH = 20000;
	public static float DIAMETER = 135;
	public static float TURN_SPEED = 0.0023f;
	public static float ACCELERATION = 3f;
	public static float MAXSPEED = 6f;
	public static Type[] TARGET_PRIORITIES = { };

	protected Factory() {
		super(Type.FACTORY, TARGET_PRIORITIES, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION, MAXSPEED);
	}

	@Override
	public boolean canShoot() {
		return false;
	}
	
	@Override
	public void updateHeading(){
		heading -= TURN_SPEED;
	}
}
