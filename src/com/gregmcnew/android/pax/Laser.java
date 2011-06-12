package com.gregmcnew.android.pax;

public class Laser extends Entity {

	public static int HEALTH = 4000;
	public static float DIAMETER = 3;
	public static float TURN_SPEED = 0;
	public static float ACCELERATION = 0;
	public static Type[] TARGET_PRIORITIES = { };

	protected Laser(int id, Entity parent) {
		super(id, Type.LASER, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION);
		location.set(parent.location);
		velocity.set(parent.velocity);
		velocity.offset(1, 1);
		heading = parent.heading;
	}
}
