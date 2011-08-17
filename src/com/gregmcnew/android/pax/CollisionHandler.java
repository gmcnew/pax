package com.gregmcnew.android.pax;

import android.util.Log;

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
		
		int damage = projectile.health;
		projectile.health = 0;
		
		if (projectile.type == Projectile.MISSILE && victimShip.type == Ship.BOMBER) {
			// Frigates shouldn't be that good against bombers.
			damage /= 4;
		}
		
		// This needs to be called before damage is applied.
		projectile.addHitParticle(mVictim, victimShip, damage);
		
		victimShip.health -= damage;
		
		if (victimShip.health <= 0) {
			result |= REMOVE_A;
			//mVictim.mEntities[victimShip.type].remove(victimShip);
			GameSounds.play(GameSounds.Sound.EXPLOSION);
		}
		
		return result;
	}
}
