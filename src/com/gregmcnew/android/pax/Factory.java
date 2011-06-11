package com.gregmcnew.android.pax;

public class Factory extends Ship {
	
	public static int HEALTH = 20000;
	public static float SIZE = 135;
	public static float TURN_SPEED = 0.00028f;
	public static float ACCELERATION = 0.002f;
	public static ShipType[] TARGET_PRIORITIES = { };
	
	// TODO: Consider adding ShipType.BOMBER as the lowest-priority target and
	// reducing the accuracy of frigate missiles when homing in on bombers.

	protected Factory(int id) {
		super(id, ShipType.FACTORY, HEALTH, SIZE, TURN_SPEED, ACCELERATION);
	}

	@Override
	public ShipType[] getTargetPriorities() {
		return TARGET_PRIORITIES;
	}
}
