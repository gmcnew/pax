package com.gregmcnew.android.pax;

public class Frigate extends Ship {
	
	public static int HEALTH = 1400;
	public static float DIAMETER = 45;
	public static float TURN_SPEED = 0.01f;
	public static float ACCELERATION = 8f;
	public static float MAXVELOCITY = 20f;
	public static Type[] TARGET_PRIORITIES = { Type.FIGHTER, Type.FRIGATE, Type.FACTORY };
	
	// TODO: Consider adding ShipType.BOMBER as the lowest-priority target and
	// reducing the accuracy of frigate missiles when homing in on bombers.

	protected Frigate(int id) {
		super(id, Type.FRIGATE, TARGET_PRIORITIES, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION, MAXVELOCITY);
	}

	@Override
	public boolean canShoot() {
		return false;
	}
	
	@Override
	public void updateCourse(){
		
	}
}
