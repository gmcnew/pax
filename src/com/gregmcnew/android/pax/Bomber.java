package com.gregmcnew.android.pax;

public class Bomber extends Ship {
	
	public static final int HEALTH = 250;
	public static final float DIAMETER = 18;
	public static final float TURN_SPEED = 0.75f; // in radians per second
	public static final float[] ACCELERATIONLIMS = {30f, 15f};
	public static final float MAXSPEED = 60f;
	public static final int[] TARGET_PRIORITIES = { Entity.FRIGATE, Entity.FACTORY, Entity.BOMBER, Entity.FIGHTER };

	protected Bomber() {
		super(Entity.BOMBER, TARGET_PRIORITIES, null, HEALTH, DIAMETER, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);

		reloadTimeMs = 4000;
		clipSize = 1;
		shotsLeft = 1;
	}
}
