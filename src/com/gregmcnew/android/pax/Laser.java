package com.gregmcnew.android.pax;

public class Laser extends Projectile {

	public static int HEALTH = 40;
	public static float DIAMETER = 3;
	public static float TURN_SPEED = 0;
	public static float ACCELERATION = 0;
	public static Type[] TARGET_PRIORITIES = { };
	public static int MAX_LIFE_MS = 1000;

	protected Laser(int id, Ship parent) {
		super(id, Type.LASER, MAX_LIFE_MS, HEALTH, DIAMETER, TURN_SPEED, ACCELERATION);
		body.center.set(parent.body.center);
		velocity.set(parent.velocity);
		velocity.offset(1, 1);
		heading = parent.heading;
	}
}