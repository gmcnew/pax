package com.gregmcnew.android.pax;

public class Frigate extends Ship {
	
	public static int HEALTH = 1400;
	public static float SIZE = 45;
	public static float TURN_SPEED = 0.01f;
	public static float ACCELERATION = 0.01f;
	public static ShipType[] TARGET_PRIORITIES = { ShipType.FIGHTER, ShipType.FRIGATE, ShipType.FACTORY };
	
	// TODO: Consider adding ShipType.BOMBER as the lowest-priority target and
	// reducing the accuracy of frigate missiles when homing in on bombers.

	protected Frigate(int id) {
		super(id, HEALTH, SIZE, TURN_SPEED, ACCELERATION);
	}

	@Override
	public ShipType[] getTargetPriorities() {
		return TARGET_PRIORITIES;
	}
}
