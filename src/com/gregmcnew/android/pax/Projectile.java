package com.gregmcnew.android.pax;

public abstract class Projectile extends Entity {

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

		if (type == BOMB) {
			victim.mEmitters[Emitter.BOMB_HIT].add(Bomb.DIAMETER,
					body.center.x, body.center.y,
					victimShip.velocity.x, victimShip.velocity.y);
		}
		
		if (damage >= victimShip.health) {
			int particles = 3 << victimShip.type;
			float radius = victimShip.radius;
			explode(victimShip, victim.mEmitters[Emitter.SMOKE], particles, radius * 3, 3);
			explode(victimShip, victim.mEmitters[Emitter.SHIP_EXPLOSION], particles, radius, 2);
		}
	}

	private void explode(Ship victimShip, Emitter emitter, int particles, float radius, float lifeMultiplier) {
		for (int i = 0; i < particles; i++) {

			float r1 = Game.sRandom.nextFloat();
			float d = r1 * victimShip.radius;
			float angle = Game.sRandom.nextFloat() * (float)Math.PI * 2;
			float dx = d * (float)Math.cos(angle);
			float dy = d * (float)Math.sin(angle);
			float x = victimShip.body.center.x + dx;
			float y = victimShip.body.center.y + dy;
			float life = (1f - r1) * lifeMultiplier;

			emitter.addVariable(radius, x, y, victimShip.velocity.x + dx, victimShip.velocity.y + dy, life);
		}
	}
}
