package com.gregmcnew.android.pax;

public class Projectile extends Entity {
	
	public static Type[] TYPES = { Entity.Type.LASER, Entity.Type.BOMB, Entity.Type.MISSILE };

	public int lifeMs;
	
	protected Projectile(Type type, Type[] targetPriorities, float[] targetSearchLimits, int LifeMs, int health, float diameter, float turnSpeed, float acceleration, float maxVelocity) {
		super(type, targetPriorities, targetSearchLimits, health, diameter, turnSpeed, acceleration, maxVelocity);
		lifeMs = LifeMs;
	}
	
	public void attack(Player victim) {

		// TODO: Fix the case in which the projectile intersects with two ships
		// (of different types) and kills the first one it finds (when it
		// actually should have hit the other ship first).
		for (Entity.Type type : Ship.TYPES) {
			int id = victim.mBodies.get(type).collide(body.center.x, body.center.y, body.radius);
			if (id != Entity.NO_ENTITY) {
				Ship target = (Ship) victim.mEntities.get(type).get(id);
				
				int damage = health;
				
				// XXX: Make projectiles superpowered!
				damage *= 100;
				
				health -= damage;
				target.health -= damage;
				
				if (target.health <= 0) {
					// Go ahead and remove the target from mBodies
					// so it doesn't block other projectiles.
					// Its ID won't be recycled until later, when
					// Player.updateEntities() is called.
					victim.mBodies.get(type).remove(id);
				}
				
				break; // don't examine other ship types
			}
		}
	}
}
