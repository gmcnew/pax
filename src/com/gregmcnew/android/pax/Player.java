package com.gregmcnew.android.pax;

import java.util.LinkedList;
import java.util.Queue;

public class Player {

	public enum BuildTarget { FIGHTER, BOMBER, FRIGATE, UPGRADE, NONE }
	
	// Cache the result of BuildTarget.values(), since it returns an array.
	public static final BuildTarget[] sBuildTargetValues = BuildTarget.values();
	
	public static final int[] BuildCosts = { 50, 170, 360, 1080, 0 };

	
	// Production is measured in money per second and is determined by the
	// equation: mProductionStepSize * mNumProductionSteps * mProductionMultiplier
	
	// mNumProductionSteps is incremented every time the player upgrades.
	private float mNumProductionSteps;
	
	// mProductionStepSize changes in response to the game speed setting.
	private float mProductionStepSize;
	
	// mProductionMultiplier changes in response to the AI difficulty setting.
	private float mProductionMultiplier;
	
	private static final int INITIAL_PRODUCTION_STEP_SIZE = 20;
	private static final int INITIAL_NUM_PRODUCTION_STEPS = 3;
	
	public float[] getAIWeightParameters() {
		return mAI.mWeightParameters;
	}
	
	private AI mAI;
	
	public void setGameSpeed(Game.Speed speed) {
		switch (speed) {
			case NORMAL:
			default:
				mProductionStepSize = 20;
				break;
			case FAST:
				mProductionStepSize = 60;
				break;
			case INSANE:
				mProductionStepSize = 120;
				break;
		}
	}
	
	public void setAIDifficulty(AI.Difficulty difficulty) {
		mAI.setDifficulty(difficulty);
	}
	
	public void updateAI(Player[] allPlayers) {
		if (mIsAI) {
			mAI.update(allPlayers);
		}
	}
	
	private boolean mIsAI;
	
	// Public methods
	
	public boolean isAI() {
		return mIsAI;
	}
	
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
		
		playerNo = playerNumber;
		totalPlayers = players;
		
		mAI = new AI(this);
		mIsAI = false;
		
		reset();
	}
	
	public void setAI(boolean ai) {
		mIsAI = ai;
	}
	
	/**Removes all of a player's ships and projectiles and generates a new factory for that player.**/
	public void reset() {
		for (int type : Entity.TYPES) {
			mEntities[type].clear();
		}
		
		mBuildTarget = BuildTarget.NONE;
		
		setAI(mIsAI);
		
		mRetargetQueue.clear();
		mShooterQueue.clear();
		money = 0;
		
		mProductionStepSize = INITIAL_PRODUCTION_STEP_SIZE;
		mNumProductionSteps = INITIAL_NUM_PRODUCTION_STEPS;
		mProductionMultiplier = 1.0f;
		
		addShip(Entity.FACTORY);
	}
	
	public boolean hasLost() {
		return mEntities[Entity.FACTORY].isEmpty();
	}
	
	public void produce(long dt) {
		money += (mProductionStepSize * mNumProductionSteps * mProductionMultiplier * dt) / 1000;
	}
	
	public void removeDeadEntities() {
		
		for (int type : Entity.TYPES) {
			for (Entity entity : mEntities[type]) {
				if (entity.health <= 0) {
					mEntities[entity.type].remove(entity);
				}
			}
		}
	}
	
	// This function requires a valid collision space (for retargeting),
	// so it can't add or move units. See moveEntities for that sort of thing.
	// Dead entities should already have been removed by a call to
	// removeDeadEntities().
	public void updateEntities(long dt) {
		
		for (int type : Entity.TYPES) {
			for (Entity entity : mEntities[type]) {
				
				entity.updateHeading(dt);
				entity.updateVelocity(dt);
				
				if (entity.target != null && entity.target.health <= 0) {
					entity.target = null;
				}
				
				if (entity.wantsNewTarget()) {
					mRetargetQueue.add(entity);
				}
				
				if (type == Entity.FIGHTER || type == Entity.BOMBER || type == Entity.FRIGATE || type == Entity.FACTORY) {
				
					Ship ship = (Ship) entity;
					if (ship.shoot(dt)) {
						mShooterQueue.add(ship);
					}
				}
				
				if (entity.lifeMs != Entity.INFINITE_LIFE_MS) {
					entity.lifeMs -= dt;
					if (entity.lifeMs <= 0) {
						entity.health = 0;
					}
				}
			}
		}
		
		if (Pax.PARTICLES) {
			for (int emitterType : Emitter.TYPES) {
				mEmitters[emitterType].update(dt);
			}
		}
	}
	
	public void moveEntities(long dt) {
		for (int type : Entity.TYPES) {
			for (Entity entity : mEntities[type]) {
				
				if (Pax.PARTICLES && type == Projectile.MISSILE) {
					Missile missile = (Missile) entity;
					missile.smoke(mEmitters[Emitter.SMOKE], dt);
				}
				
				entity.updatePosition(dt);
			}
		}
		
		for (Ship ship : mShooterQueue) {
			addProjectile(ship);
		}
		mShooterQueue.clear();
	}
	
	public void attack(Player victim) {
		for (int type : Projectile.TYPES) {
			for (Entity entity : mEntities[type]) {
				
				Projectile projectile = (Projectile) entity;
				
				if (projectile.health >= 0) {
					// This projectile may be dead (from a previous attack),
					// in which case it will be removed later.
					projectile.attack(victim);
				}
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
				mNumProductionSteps++;
				break;
		}
		
		if (mIsAI) {
			// This triggers the AI to determine a new build target. 
			mBuildTarget = BuildTarget.NONE;
		}
	}
	
	private Ship addShip(int type) {
		
		Ship ship = (Ship) mEntities[type].add(type, null);
		
		if (ship != null) {
			
			// Fix the ship's location.
			if (type != Entity.FACTORY) { // If the ship being spawned ISN'T a factory...
				Ship factory = (Ship) mEntities[Entity.FACTORY].get(0);
				float spawnX = factory.body.center.x + (float) (Factory.DIAMETER * 0.4 * Math.cos(factory.heading));
				float spawnY = factory.body.center.y + (float) (Factory.DIAMETER * 0.4 * Math.sin(factory.heading));
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
		
		return ship;
	}
	
	private Projectile addProjectile(Ship parent) {
		
		int projectileType;
		switch (parent.type) {
			case Entity.FIGHTER:
				projectileType = Entity.LASER;
				break;
			case Entity.BOMBER:
				projectileType = Entity.BOMB;
				break;
			default:
			case Entity.FRIGATE:
				projectileType = Entity.MISSILE;
				break;
		}
		
		Projectile projectile = (Projectile) mEntities[projectileType].add(projectileType, parent);
		
		return projectile;
	}
	
	public EntityPool[] mEntities;
	public Emitter[] mEmitters;
	
	// The retarget queue contains entities that want a new target. This queue
	// should be handled and cleared on every call to Game.update().
	public Queue<Entity> mRetargetQueue;
	public Queue<Ship> mShooterQueue;
	
	public float money;
	public BuildTarget mBuildTarget;
	public int playerNo;
	public int totalPlayers;
}
