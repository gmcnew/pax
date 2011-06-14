package com.gregmcnew.android.pax;

public class Bomber extends Ship {
	
	public static int HEALTH = 250;
	public static float DIAMETER = 18;
	public static float TURN_SPEED = 0.03f;
	public static float ACCELERATION = 30f;
	public static float MAXSPEED = 60f;
	public static Type[] TARGET_PRIORITIES = { Type.FRIGATE, Type.FACTORY, Type.BOMBER, Type.FIGHTER };

	protected Bomber() {
		super(Type.BOMBER, TARGET_PRIORITIES, null, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION, MAXSPEED);
	}

	@Override
	public boolean canShoot() {
		return false;
	}
	
	@Override
	public void updateHeading(){
		
	}
}
