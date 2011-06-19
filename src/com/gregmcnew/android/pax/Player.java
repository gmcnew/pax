package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Player {

	public enum BuildTarget { FIGHTER, BOMBER, FRIGATE, UPGRADE, NONE }
	public static final int[] BuildCosts = { 50, 170, 360, 1080, 0 };
	
	
	// Public methods
	
	public Player(int playerNumber, int players) {
		mBuildTarget = BuildTarget.NONE;
		money = 0;
		production = 0.75f;
		production *= 5; // warp speed!
		
		mEntities = new EntityPool[Entity.TYPES.length];
		
		mRetargetQueue = new LinkedList<Entity>();
		mShooterQueue = new LinkedList<Ship>();
		
		for (int type : Entity.TYPES) {
			mEntities[type] = new EntityPool(type);
		}
		
		playerNo = playerNumber;
		totalPlayers = players;
		reset();
	}
	
	/**Removes all of a player's ships and projectiles and generates a new factory for that player.**/
	public void reset() {
		for (int type : Entity.TYPES) {
			mEntities[type].clear();
		}
		
		mRetargetQueue.clear();
		
		addShip(Entity.FACTORY);
	}
	
	public boolean hasLost() {
		return mEntities[Entity.FACTORY].isEmpty();
	}
	
	public void produce() {
		money += production;
	}
	
	// This function requires a valid collision space (for retargeting),
	// so it can't add or move units. See moveEntities for that sort of thing.
	public void updateEntities() {
		
		for (int type : Entity.TYPES) {
			for (Entity entity : mEntities[type]) {
				
				if (entity.wantsNewTarget()) {
					mRetargetQueue.add(entity);
				}
				
				if (type == Entity.FIGHTER || type == Entity.BOMBER || type == Entity.FRIGATE || type == Entity.FACTORY) {
				
					Ship ship = (Ship) entity;
					if (ship.health <= 0) {
						removeEntity(ship);
					}
					else {
						if (ship.canShoot()) {
							switch (ship.type){
							case Entity.FIGHTER:
								ship.shotsLeft = 5;
								break;
							case Entity.BOMBER:
								ship.shotsLeft = 1;
								break;
							case Entity.FRIGATE:
								ship.shotsLeft = 10;
								break;
							}
							mShooterQueue.add(ship);
						}
					}
				}
				else { // it's a projectile
					Projectile projectile = (Projectile) entity;
					
					if (projectile.health <= 0 || projectile.lifeMs <= 0) {
						removeEntity(projectile);
					}
					else {
						projectile.lifeMs -= Pax.UPDATE_INTERVAL_MS;
					}
				}
			}
		}
	}
	
	public void moveEntities() {
		for (int type : Entity.TYPES) {
			for (Entity entity : mEntities[type]) {
				entity.move();
			}
		}
		
		ArrayList<Ship> toBeRemoved = new ArrayList<Ship>(); //The list of ships that need to be removed from the queue.
		
		for (Ship ship : mShooterQueue) {
			if(ship.updatesSinceShot <= ship.shotInterval){
				ship.updatesSinceShot++;
			}
			else{
				addProjectile(ship);
				ship.updatesSinceShot = 0;
			}
			if(ship.shotsLeft == 0){
				toBeRemoved.add(ship);
			}
		}
		mShooterQueue.removeAll(toBeRemoved);
	}
	
	public void attack(Player victim) {
		for (int type : Projectile.TYPES) {
			for (Entity entity : mEntities[type]) {
				Projectile projectile = (Projectile) entity;
				
				projectile.attack(victim);

				// The projectile will be removed elsewhere if it's dead now.
			}
		}
	}
	
	public void build() {
		if (mBuildTarget != BuildTarget.NONE) {
			int cost = BuildCosts[mBuildTarget.ordinal()]; 
			if (money >= cost) {
				build(mBuildTarget);
				money -= cost;
			}
		}
	}
	
	public void invalidateCollisionSpaces() {
		for (int type : Entity.TYPES) {
			mEntities[type].invalidateCollisionSpaces();
		}
	}
	
	public void rebuildCollisionSpaces() {

		for (int type : Entity.TYPES) {
			mEntities[type].rebuildCollisionSpaces();
		}
	}
	
	
	// Private methods
	
	private void build(BuildTarget buildTarget) {
		switch (buildTarget) {
			case FIGHTER:
				addShip(Entity.FIGHTER);
				break;
			case BOMBER:
				addShip(Entity.BOMBER);
				break;
			case FRIGATE:
				addShip(Entity.FRIGATE);
				break;
			case UPGRADE:
				production += 0.25f;
				break;
		}
	}
	
	private int addShip(int type) {
		
		int id = Entity.NO_ENTITY;
			
		Ship ship = null;
		switch (type) {
			case Entity.FIGHTER:
				ship = new Fighter();
				break;
			case Entity.BOMBER:
				ship = new Bomber();
				break;
			case Entity.FRIGATE:
				ship = new Frigate();
				break;
			case Entity.FACTORY:
				ship = new Factory();
				break;
		}
		
		if (ship != null) {
			id = mEntities[type].add(ship);
			
			// Fix the ship's location.
			if (type != Entity.FACTORY) { // If the ship being spawned ISN'T a factory...
				Ship factory = (Ship) mEntities[Entity.FACTORY].get(0);
				float spawnX, spawnY;
				spawnX = factory.body.center.x + (float) (55 * Math.cos(factory.heading));
				spawnY = factory.body.center.y + (float) (55 * Math.sin(factory.heading));
				ship.body.center.set(spawnX, spawnY);
				ship.heading = factory.heading;
			}
			else { // If the ship being spawned IS a factory...
				float offset = (float) Math.PI/40; // The larger this value, the faster the factories will converge.
				
				float orbitRadius = 320 / 3; // The radius that the factory will orbit the center at.
				float spacing = (float)(2*Math.PI / totalPlayers);// The spacing in radians between the factories.
				float theta = spacing*(float)(playerNo);// The angle in radians at which this particular factory will be spawned.
				
				float factoryX = (float) (orbitRadius * Math.cos(theta));
				float factoryY = (float) (orbitRadius * Math.sin(theta));
				
				ship.body.center.set(factoryX, factoryY);
				ship.heading = theta - (float) Math.PI/2 - offset;
			}
		}
		
		return id;
	}
	
	private int addProjectile(Ship parent) {
		
		int id = Entity.NO_ENTITY;
		
		Projectile projectile = null;
		switch (parent.type) {
			case Entity.FIGHTER:
				projectile = new Laser(parent);
				break;
			case Entity.BOMBER:
				projectile = new Bomb(parent);
				break;
			case Entity.FRIGATE:
				projectile = new Missile(parent);
				break;
		}
		
		if (projectile != null) {
			id = mEntities[projectile.type].add(projectile);
		}
		
		parent.shotsLeft--;
		
		return id;
	}
	
	private void removeEntity(Entity entity) {
		mEntities[entity.type].remove(entity.id);
	}
	
	public EntityPool[] mEntities;
	
	// The retarget queue contains entities that want a new target. This queue
	// should be handled and cleared on every call to Game.update().
	public Queue<Entity> mRetargetQueue;
	public Queue<Ship> mShooterQueue;
	
	public float money;
	public float production;
	public BuildTarget mBuildTarget;
	public int playerNo;
	public int totalPlayers;
}
