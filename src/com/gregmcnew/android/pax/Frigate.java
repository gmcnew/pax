package com.gregmcnew.android.pax;

public class Frigate extends Ship {
	
	public static final int HEALTH = 1400;
	public static final float DIAMETER = 45;
	public static final float TURN_SPEED = 0.01f;
	public static final float[] ACCELERATIONLIMS = {30f, 15f};
	public static final float MAXSPEED = 20f;
	public static final int[] TARGET_PRIORITIES = { Entity.FIGHTER, Entity.FRIGATE, Entity.FACTORY };
	public static final int shotInterval = 3;
	
	// TODO: Consider adding ShipType.BOMBER as the lowest-priority target and
	// reducing the accuracy of frigate missiles when homing in on bombers.

	protected Frigate() {
		super(Entity.FRIGATE, TARGET_PRIORITIES, null, HEALTH, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);
	}

	@Override
	public boolean canShoot() {
		if(shotsLeft > 0) return false;
		return false;
	}
}
