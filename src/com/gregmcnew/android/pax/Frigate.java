package com.gregmcnew.android.pax;

public class Frigate extends Ship {
	
	public static int HEALTH = 1400;
	public static float DIAMETER = 45;
	public static float TURN_SPEED = 0.01f;
	public static float MAXACCELERATION = 8f;
	public static float MAXSPEED = 20f;
	public static Type[] TARGET_PRIORITIES = { Type.FIGHTER, Type.FRIGATE, Type.FACTORY };
	
	// TODO: Consider adding ShipType.BOMBER as the lowest-priority target and
	// reducing the accuracy of frigate missiles when homing in on bombers.

	protected Frigate(int id) {
		super(id, Type.FRIGATE, HEALTH, DIAMETER, TURN_SPEED, MAXACCELERATION, MAXSPEED);
	}

	@Override
	public Type[] getTargetPriorities() {
		return TARGET_PRIORITIES;
	}

	@Override
	public boolean canShoot() {
		return false;
	}
	
	@Override
	public void updateHeading(){
		
	}
}
