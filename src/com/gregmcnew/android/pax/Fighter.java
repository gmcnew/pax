package com.gregmcnew.android.pax;

public class Fighter extends Ship {
	
	public static int HEALTH = 40;
	public static float SIZE = 7.5f;
	public static float TURN_SPEED = 0.025f;
	public static float ACCELERATION = 0.1f;
	public static ShipType[] TARGET_PRIORITIES = { ShipType.BOMBER, ShipType.FIGHTER, ShipType.FRIGATE, ShipType.FACTORY };

	protected Fighter(int id) {
		super(id, HEALTH, SIZE, TURN_SPEED, ACCELERATION);
	}

	@Override
	public ShipType[] getTargetPriorities() {
		return TARGET_PRIORITIES;
	}
}
