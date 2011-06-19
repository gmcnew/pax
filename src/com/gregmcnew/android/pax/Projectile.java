package com.gregmcnew.android.pax;

public class Projectile extends Entity {

	@SuppressWarnings("hiding")
	public static final int[] TYPES = { Entity.LASER, Entity.BOMB, Entity.MISSILE };

	public int lifeMs;
	
	protected Projectile(int type, int[] targetPriorities, float[] targetSearchLimits, int LifeMs, int health, float diameter, float turnSpeed, float accelerationLimits[], float maxVelocity) {
		super(type, targetPriorities, targetSearchLimits, health, diameter, turnSpeed, accelerationLimits, maxVelocity);
		lifeMs = LifeMs;
	}
	
	public void attack(Player victim) {

		// TODO: Fix the case in which the projectile intersects with two ships
		// (of different types) and kills the first one it finds (when it
		// actually should have hit the other ship first).
		for (int victimShipType : Ship.TYPES) {
			Ship victimShip = (Ship) victim.mEntities[victimShipType].collide(body.center, body.radius);
			if (victimShip != null) {
				
				int damage = health;
				
				// XXX: Make projectiles superpowered!
				//damage *= 10;
				
				health -= damage;
				victimShip.health -= damage;
				
				if (victimShip.health <= 0) {
					victim.mEntities[victimShipType].remove(victimShip);
				}
				
				break; // don't examine other ship types
			}
		}
	}
}
