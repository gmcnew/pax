package com.gregmcnew.android.pax;

import java.util.LinkedList;
import java.util.Queue;

public class Player {

	public enum BuildTarget { FIGHTER, BOMBER, FRIGATE, UPGRADE, NONE }
	public static final int[] BuildCosts = { 50, 170, 360, 1080, 0 };

	// Production is measured in money per second.
	private static final float PRODUCTION_STEP = 20;
	private static final float INITIAL_PRODUCTION = PRODUCTION_STEP * 3;
	
	// Spend rougly equal amounts of money on all ship types, but never upgrade.
	public static final Player.BuildTarget[] AI_BUILD_TARGETS = {
			Player.BuildTarget.FIGHTER, Player.BuildTarget.FIGHTER,
			Player.BuildTarget.BOMBER,
			Player.BuildTarget.FIGHTER, Player.BuildTarget.FIGHTER,
			Player.BuildTarget.BOMBER,
			Player.BuildTarget.FIGHTER, Player.BuildTarget.FIGHTER, Player.BuildTarget.FIGHTER,
			Player.BuildTarget.FRIGATE,
			};
	private int mNextAIBuildTarget;
	private boolean mIsAI;
	
	// Public methods
	
	public Player(int playerNumber, int players) {
		
		mEntities = new EntityPool[Entity.TYPES.length];
		
		mRetargetQueue = new LinkedList<Entity>();
		mShooterQueue = new LinkedList<Ship>();
		
		for (int type : Entity.TYPES) {
			mEntities[type] = new EntityPool(type);
		}
		
		mEmitters = new Emitter[Emitter.TYPES.length];
		for (int type : Emitter.TYPES) {
			mEmitters[type] = new Emitter(type);
		}
		
		mBuildTarget = BuildTarget.NONE;
		
		playerNo = playerNumber;
		totalPlayers = players;
		mIsAI = false;
		reset();
	}
	
	public void setAI(boolean ai) {
		mIsAI = ai;
		
		if (mIsAI) {
			mNextAIBuildTarget = Pax.sRandom.nextInt(AI_BUILD_TARGETS.length);
			mBuildTarget = AI_BUILD_TARGETS[mNextAIBuildTarget];
		}
	}
	
	/**Removes all of a player's ships and projectiles and generates a new factory for that player.**/
	public void reset() {
		for (int type : Entity.TYPES) {
			mEntities[type].clear();
		}
		
		setAI(mIsAI);
		
		mRetargetQueue.clear();
		mShooterQueue.clear();
		money = 0;
		production = INITIAL_PRODUCTION;
		
		addShip(Entity.FACTORY);
	}
	
	public boolean hasLost() {
		return mEntities[Entity.FACTORY].isEmpty();
	}
	
	public void produce(long dt) {
		money += (production * dt) / 1000;
	}
	
	// This function requires a valid collision space (for retargeting),
	// so it can't add or move units. See moveEntities for that sort of thing.
	public void updateEntities(long dt) {
		
		for (int type : Entity.TYPES) {
			for (Entity entity : mEntities[type]) {
				
				if (entity.target != null && entity.target.health <= 0) {
					entity.target = null;
				}
				
				if (entity.wantsNewTarget()) {
					mRetargetQueue.add(entity);
				}
				
				if (type == Entity.FIGHTER || type == Entity.BOMBER || type == Entity.FRIGATE || type == Entity.FACTORY) {
				
					Ship ship = (Ship) entity;
					if (ship.health <= 0) {
						removeEntity(ship);
					}
					else {
						if (ship.shoot(dt)) {
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
						projectile.lifeMs -= dt;
					}
				}
			}
		}
	}
	
	public void moveEntities(long dt) {
		for (int type : Entity.TYPES) {
			for (Entity entity : mEntities[type]) {
				
				if (Pax.PARTICLES) {
					if (type == Projectile.MISSILE) {
						mEmitters[Emitter.SMOKE].add(1f, entity.body.center.x, entity.body.center.y,
								(Pax.sRandom.nextFloat() - 0.5f) * 40,
								(Pax.sRandom.nextFloat() - 0.5f) * 40);
					}
				}
				
				entity.move(dt);
			}
		}
		
		for (Ship ship : mShooterQueue) {
			addProjectile(ship);
		}
		mShooterQueue.clear();
	}
	
	public void updateParticles(long dt) {
		if (Pax.PARTICLES) {
			for (int emitterType : Emitter.TYPES) {
				mEmitters[emitterType].update(dt);
			}
		}
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
			
			// We don't want to build more than one ship per frame, even if we
			// can afford it, because this results in the ships being stacked on
			// top of each other. Ship AI is deterministic, so they'll always
			// stay on top of each other, acting like a super-ship but looking
			// like a single ship.
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
				production += PRODUCTION_STEP;
				break;
		}
		
		if (mIsAI) {
			mNextAIBuildTarget++;
			mNextAIBuildTarget %= AI_BUILD_TARGETS.length;
			mBuildTarget = AI_BUILD_TARGETS[mNextAIBuildTarget];
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
				
				float orbitRadius = GameRenderer.GAME_VIEW_SIZE / 4; // The radius that the factory will orbit the center at.
				float spacing = (float)(2*Math.PI / totalPlayers);// The spacing in radians between the factories.
				float theta = spacing*(float)(playerNo) - (float) (Math.PI / 2);// The angle in radians at which this particular factory will be spawned.
				
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
		
		return id;
	}
	
	private void removeEntity(Entity entity) {
		// Clear our target reference for garbage collection reasons.
		entity.target = null;
		
		mEntities[entity.type].remove(entity.id);
	}
	
	public EntityPool[] mEntities;
	public Emitter[] mEmitters;
	
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
