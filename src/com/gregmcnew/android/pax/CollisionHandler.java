package com.gregmcnew.android.pax;

public class CollisionHandler {

	private Player mVictim;
	private EntityPool mShipPool;
	private EntityPool mProjectilePool;
	private boolean mFlipArgs;
	
	public static int REMOVE_NONE = 0;
	public static int REMOVE_A    = 1;
	public static int REMOVE_B    = 2;
	public static int REMOVE_BOTH = REMOVE_A | REMOVE_B;
	
	public CollisionHandler() {
		mVictim = null;
		mShipPool = null;
		mProjectilePool = null;
		mFlipArgs = false;
	}
	
	public void initialize(Player victim, EntityPool shipPool, EntityPool projectilePool, boolean flipArgs) {
		mFlipArgs = flipArgs;
		mVictim = victim;
		mShipPool       = shipPool;
		mProjectilePool = projectilePool;
	}

	public int collide(Point2 a, Point2 b) {
		
		//if (a.id < 0 || b.id < 0)
		{
			//Log.v("", String.format("colliding with points with IDs %d and %d", a.id, b.id));
		}
		
		Ship victimShip = (Ship) mShipPool.get(mFlipArgs ? b.id : a.id);
		Projectile projectile = (Projectile) mProjectilePool.get(mFlipArgs ? a.id : b.id);
	
		/*
		Ship victimShip = (Ship) victim.mEntities[victimShipType].collide(body.center, body.radius);

		// Check other collision points if necessary.
		for (int i = 0; i < mExtraPoints.length && victimShip == null; i++) {
			victimShip = (Ship) victim.mEntities[victimShipType].collide(mExtraPoints[i], body.radius);
		}
		
		if (victimShip != null) {
		*/
		
		int result = REMOVE_NONE;
		
		result |= REMOVE_B;
		
		int damage = 0;
		
		// Don't deal damage in benchmark mode. This preserves determinism when
		// collision code is changed. Different collision code can result in
		// ships and projectiles being enumerated differently, which causes
		// different collisions, which causes different games to unfold.
		// (For example, if lasers A and B have both intersected a ship since
		// the last game step, and if either will kill the ship, we're not
		// guaranteed which laser will hit the ship and which will keep going.
		// The collision code gets to decide.)
		if (!Pax.sBenchmarkMode) {
			damage = projectile.health;
			projectile.health = 0;
		}
		
		if (projectile.type == Projectile.MISSILE && victimShip.type == Ship.BOMBER) {
			// Frigates shouldn't be that good against bombers.
			damage /= 4;
		}
		
		// This needs to be called before damage is applied.
		projectile.addHitParticle(mVictim, victimShip, damage);
		
		// Factory damage resistance can vary.
		if (victimShip.type == Entity.FACTORY)
		{
			damage *= mVictim.getFactoryDamageMultiplier();
		}
		
		victimShip.health -= damage;
		
		if (victimShip.health <= 0) {
			result |= REMOVE_A;
			//mVictim.mEntities[victimShip.type].remove(victimShip);
			GameSounds.play(GameSounds.Sound.EXPLOSION);
		}
		
		return result;
	}
}
