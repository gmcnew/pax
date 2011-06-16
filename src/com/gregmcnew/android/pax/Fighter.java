package com.gregmcnew.android.pax;

public class Fighter extends Ship {
	
	public static int HEALTH = 40;
	public static float DIAMETER = 7.5f;
	public static float TURN_SPEED = 0.025f; //in radians per 40ms
	public static float[]ACCELERATIONLIMS = {120f, 60f};
	public static float MAXSPEED = 150f;
	public static int[] TARGET_PRIORITIES = { Entity.BOMBER, Entity.FIGHTER, Entity.FRIGATE, Entity.FACTORY };

	protected Fighter() {
		super(Entity.FIGHTER, TARGET_PRIORITIES, null, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);
	}

	@Override
	public boolean canShoot() {
		return Math.random() > 0.99f;
	}
}
