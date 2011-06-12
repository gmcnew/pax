package com.gregmcnew.android.pax;

public class Fighter extends Entity {
	
	public static int HEALTH = 40;
	public static float DIAMETER = 7.5f;
	public static float TURN_SPEED = 0.025f;
	public static float ACCELERATION = 0.1f;
	public static Type[] TARGET_PRIORITIES = { Type.BOMBER, Type.FIGHTER, Type.FRIGATE, Type.FACTORY };

	protected Fighter(int id) {
		super(id, Type.FIGHTER, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION);
	}

	@Override
	public Type[] getTargetPriorities() {
		return TARGET_PRIORITIES;
	}
}
