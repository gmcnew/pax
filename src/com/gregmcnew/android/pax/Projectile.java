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
	
	// This should be called before damage is applied.
	public void addHitParticle(Player victim, Ship victimShip, int damage) {

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
				float x = victimShip.body.center.x + (Game.sRandom.nextFloat() * victimShip.diameter) - victimShip.radius;
				float y = victimShip.body.center.y + (Game.sRandom.nextFloat() * victimShip.diameter) - victimShip.radius;
				
				explosionEmitter.add(victimShip.radius, x, y, victimShip.velocity.x, victimShip.velocity.y);
			}
		}
	}
}
