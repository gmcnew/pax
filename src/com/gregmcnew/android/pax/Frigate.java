package com.gregmcnew.android.pax;

public class Frigate extends Ship {
	
	public static final int HEALTH = 1400;
	public static final float DIAMETER = 45;
	public static final float TURN_SPEED = 0.015f;
	public static final float[] ACCELERATIONLIMS = {10f, 5f};
	public static final float MAXSPEED = 30f;
	public static final int[] TARGET_PRIORITIES = { Entity.FIGHTER, Entity.FRIGATE, Entity.FACTORY };
	public static final int SHOT_INTERVAL = 1;
	
	// TODO: Consider adding ShipType.BOMBER as the lowest-priority target and
	// reducing the accuracy of frigate missiles when homing in on bombers.

	protected Frigate() {
		super(Entity.FRIGATE, TARGET_PRIORITIES, null, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);

		reloadTimeMs = 6000;
		shotTimeMs = 20;
		clipSize = 8;
		shotsLeft = clipSize;
	}
}
