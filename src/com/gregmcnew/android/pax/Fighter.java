package com.gregmcnew.android.pax;

public class Fighter extends Ship {
	
	public static int HEALTH = 40;
	public static float DIAMETER = 7.5f;
	public static float TURN_SPEED = 0.025f;
	public static float ACCELERATION = .5f;
	public static float MAXVELOCITY = 1.5f;
	public static Type[] TARGET_PRIORITIES = { Type.BOMBER, Type.FIGHTER, Type.FRIGATE, Type.FACTORY };

	protected Fighter(int id) {
		super(id, Type.FIGHTER, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION, MAXVELOCITY);
	}

	@Override
	public Type[] getTargetPriorities() {
		return TARGET_PRIORITIES;
	}

	@Override
	public boolean canShoot() {
		return Math.random() > 0.99f;
	}
	
	@Override
	public void updateHeading(){
		
	}
}
