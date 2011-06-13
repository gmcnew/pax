package com.gregmcnew.android.pax;

public class Projectile extends Entity {

	// XXX: Clean this stuff up. Java enums are frustrating.
	public static int LASER   = 0;
	public static int BOMB    = 1;
	public static int MISSILE = 2;
	public static int[] TYPES = { LASER, BOMB, MISSILE };

	public int lifeMs;
	
	protected Projectile(Type type, Type[] targetPriorities, int LifeMs, int health, float diameter, float turnSpeed, float acceleration, float maxVelocity) {
		super(type, targetPriorities, health, diameter, turnSpeed, acceleration, maxVelocity);
		lifeMs = LifeMs;
	}
	
	public void attack(Player victim) {

		// TODO: Fix the case in which the projectile intersects with two ships
		// (of different types) and kills the first one it finds (when it
		// actually should have hit the other ship first). 
		for (int shipType : Ship.TYPES) {
			int id = victim.mShipLayers.get(shipType).collide(body.center.x, body.center.y, body.radius);
			if (id != Game.NO_ENTITY) {
				Ship target = victim.mShipLists.get(shipType).get(id);
				
				int damage = health;
				
				// XXX: Make projectiles superpowered!
				damage *= 100;
				
				health -= damage;
				target.health -= damage;
				
				if (target.health <= 0) {
					// Go ahead and remove the target from shipBodies
					// so it doesn't block other projectiles.
					// Its ID won't be recycled until later, when
					// Player.updateEntities() is called.
					victim.mShipLayers.get(shipType).remove(id);
				}
				
				break; // don't examine other ship types
			}
		}
	}
}
