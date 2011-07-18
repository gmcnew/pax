package com.gregmcnew.android.pax;

public abstract class Projectile extends Entity {

	@SuppressWarnings("hiding")
	public static final int[] TYPES = { Entity.LASER, Entity.BOMB, Entity.MISSILE };
	
	protected Projectile(int type, int[] targetPriorities, float[] targetSearchLimits, int LifeMs, int damage, float diameter, float length, float turnSpeed, float accelerationLimits[], float maxVelocity) {
		super(type, targetPriorities, targetSearchLimits, LifeMs, damage, diameter, length, turnSpeed, accelerationLimits, maxVelocity);
	}
	
	@Override
	public void reset(Ship parent) {
		super.reset(parent);
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
				
				// This needs to be called before damage is applied.
				addHitParticle(victim, victimShip, damage);
				
				victimShip.health -= damage;
				
				if (victimShip.health <= 0) {
					victim.mEntities[victimShipType].remove(victimShip);
					GameSounds.play(GameSounds.Sound.EXPLOSION);
				}
				
				break; // don't examine other ship types
			}
		}
	}
	
	// This should be called before damage is applied.
	private void addHitParticle(Player victim, Ship victimShip, int damage) {

		int emitterType = Emitter.BOMB_HIT;
		switch (type) {
			case LASER:
				emitterType = Emitter.LASER_HIT;
				break;
			case MISSILE:
				emitterType = Emitter.MISSILE_HIT;
				break;
			case BOMB:
				emitterType = Emitter.BOMB_HIT;
				break;
		}

		victim.mEmitters[emitterType].add(1f,
				body.center.x, body.center.y,
				victimShip.velocity.x, victimShip.velocity.y);
		
		if (damage >= victimShip.health) {
			
			int particles = 1 << victimShip.type;
			
			Emitter explosionEmitter = victim.mEmitters[Emitter.SHIP_EXPLOSION];
			for (int i = 0; i < particles; i++) {
				float x = victimShip.body.center.x + (Pax.sRandom.nextFloat() * victimShip.diameter) - victimShip.radius;
				float y = victimShip.body.center.y + (Pax.sRandom.nextFloat() * victimShip.diameter) - victimShip.radius;
				
				explosionEmitter.add(victimShip.radius, x, y, victimShip.velocity.x, victimShip.velocity.y);
			}
		}
	}
}
