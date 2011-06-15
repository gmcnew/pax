package com.gregmcnew.android.pax;

public class Frigate extends Ship {
	
	public static int HEALTH = 1400;
	public static float DIAMETER = 45;
	public static float TURN_SPEED = 0.01f;
	public static float[] ACCELERATIONLIMS = {30f, 15f};
	public static float MAXSPEED = 20f;
	public static Type[] TARGET_PRIORITIES = { Type.FIGHTER, Type.FRIGATE, Type.FACTORY };
	
	// TODO: Consider adding ShipType.BOMBER as the lowest-priority target and
	// reducing the accuracy of frigate missiles when homing in on bombers.

	protected Frigate() {
		super(Type.FRIGATE, TARGET_PRIORITIES, null, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);
	}

	@Override
	public boolean canShoot() {
		return false;
	}
}
