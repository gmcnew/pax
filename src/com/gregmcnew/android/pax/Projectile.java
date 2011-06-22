package com.gregmcnew.android.pax;

public class Projectile extends Entity {

	@SuppressWarnings("hiding")
	public static final int[] TYPES = { Entity.LASER, Entity.BOMB, Entity.MISSILE };

	public int lifeMs;
	
	protected Projectile(int type, int[] targetPriorities, float[] targetSearchLimits, int LifeMs, int damage, float diameter, float turnSpeed, float accelerationLimits[], float maxVelocity) {
		super(type, targetPriorities, targetSearchLimits, damage, diameter, turnSpeed, accelerationLimits, maxVelocity);
		lifeMs = LifeMs;
	}
	
	public void attack(Player victim) {

		// TODO: Fix the case in which the projectile intersects with two ships
		// (of different types) and kills the first one it finds (when it
		// actually should have hit the other ship first).
		
		// Start with the larger ship types to improve the speed of collision
		// detection.
		for (int victimShipType : Ship.TYPES_LARGEST_FIRST) {
			Ship victimShip = (Ship) victim.mEntities[victimShipType].collide(body.center, body.radius);

			// Check other collision points if necessary.
			for (int i = 0; i < mExtraPoints.length && victimShip == null; i++) {
				victimShip = (Ship) victim.mEntities[victimShipType].collide(mExtraPoints[i], body.radius);
			}
			
			if (victimShip != null) {
				
				int damage = health;
				health = 0;
				
				if (type == MISSILE && victimShipType == BOMBER) {
					// Frigates shouldn't be that good against bombers.
					damage /= 4;
				}
				
				victimShip.health -= damage;
				
				if (victimShip.health <= 0) {
					victim.mEntities[victimShipType].remove(victimShip);
				}
				
				break; // don't examine other ship types
			}
		}
	}
}
