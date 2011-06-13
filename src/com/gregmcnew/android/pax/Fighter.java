package com.gregmcnew.android.pax;

public class Fighter extends Ship {
	
	public static int HEALTH = 40;
	public static float DIAMETER = 7.5f;
	public static float TURN_SPEED = 0.025f; //in radians per 40ms
	public static float ACCELERATION = 120f;
	public static float MAXSPEED = 150f;
	public static Type[] TARGET_PRIORITIES = { Type.BOMBER, Type.FIGHTER, Type.FRIGATE, Type.FACTORY };

	protected Fighter() {
		super(Type.FIGHTER, TARGET_PRIORITIES, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION, MAXSPEED);
	}

	@Override
	public boolean canShoot() {
		return Math.random() > 0.99f;
	}
	
	@Override
	public void updateHeading(){
		
	}
}
