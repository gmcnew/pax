package com.gregmcnew.android.pax;

public class Bomber extends Ship {
	
	public static int HEALTH = 250;
	public static float DIAMETER = 18;
	public static float TURN_SPEED = 0.03f;
	public static float ACCELERATION = 30f;
	public static float MAXVELOCITY = 60f;
	public static Type[] TARGET_PRIORITIES = { Type.FRIGATE, Type.FACTORY, Type.BOMBER, Type.FIGHTER };

	protected Bomber(int id) {
		super(id, Type.BOMBER, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION, MAXVELOCITY);
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
		
	}
}
