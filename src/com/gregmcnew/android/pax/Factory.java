package com.gregmcnew.android.pax;

public class Factory extends Ship {
	
	public static int HEALTH = 20000;
	public static float DIAMETER = 135;
	public static float TURN_SPEED = 0.0023f;
	public static float ACCELERATION = 3f;
	public static float MAXVELOCITY = 6f;
	public static Type[] TARGET_PRIORITIES = { };

	protected Factory(int id) {
		super(id, Type.FACTORY, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION, MAXVELOCITY);
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
	public void updateCourse(){
		heading -= TURN_SPEED;
	}
}
