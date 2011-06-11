package com.gregmcnew.android.pax;

public class Bomber extends Ship {
	
	public static int HEALTH = 250;
	public static float SIZE = 18;
	public static float TURN_SPEED = 0.03f;
	public static float ACCELERATION = 0.05f;
	public static ShipType[] TARGET_PRIORITIES = { ShipType.FRIGATE, ShipType.FACTORY, ShipType.BOMBER, ShipType.FIGHTER };

	protected Bomber(int id) {
		super(id, ShipType.BOMBER, HEALTH, SIZE, TURN_SPEED, ACCELERATION);
	}

	@Override
	public ShipType[] getTargetPriorities() {
		return TARGET_PRIORITIES;
	}
}
