package com.gregmcnew.android.pax;

public class Player {

	public enum BuildTarget { FIGHTER, BOMBER, FRIGATE, UPGRADE, NONE }
	
	// Cache the result of BuildTarget.values(), since it returns an array.
	public static final BuildTarget[] sBuildTargetValues = BuildTarget.values();
	
	public static final int[] BuildCosts = { 50, 170, 360, 1080, 0 };

	
	// Production is measured in money per second and is determined by the
	// equation: mProductionStepSize * mNumProductionSteps * mProductionMultiplier
	
	// mNumProductionSteps is incremented every time the player upgrades.
	private float mNumProductionSteps;
	
	// mProductionMultiplier changes in response to the AI difficulty setting.
	private float mProductionMultiplier;
	
	private static final int PRODUCTION_STEP_SIZE = 20;
	private static final int INITIAL_NUM_PRODUCTION_STEPS = 3;
	
	public float[] getAIWeightParameters() {
		return mAI.mWeights;
	}
	
	private AI mAI;
	
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
		
		mRetargetQueue = new EntityVector();
		mShooterQueue = new EntityVector();
		
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
		
		mNumProductionSteps = INITIAL_NUM_PRODUCTION_STEPS;
		mProductionMultiplier = 1.0f;
		
		addShip(Entity.FACTORY);
	}
	
	public boolean hasLost() {
		return mEntities[Entity.FACTORY].isEmpty();
	}
	
	public void produce(long dt) {
		float production = PRODUCTION_STEP_SIZE * Pax.sGameSpeed * mNumProductionSteps * mProductionMultiplier;
		if (Pax.sBenchmarkMode) {
			production *= 100;
		}
		money += (production * dt) / 1000;
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
						
						// Missiles should appear to explode when they die.
						if (Entity.MISSILE == entity.type) {
							mEmitters[Emitter.SHIP_EXPLOSION].add(entity.radius,
									entity.body.center.x, entity.body.center.y,
									entity.velocity.x, entity.velocity.y,
									-1f);
						}
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
				if (Pax.PARTICLES) {
					entity.emitParticles(mEmitters, dt);
				}
				
				entity.updatePosition(dt);
			}
		}
		
		for (Entity shooter : mShooterQueue) {
			addProjectile((Ship) shooter);
		}
		mShooterQueue.clear();
	}
	
	private static CollisionHandler sCH = new CollisionHandler();
	
	public void attack(Player victim) {
		for (int shipType : Ship.TYPES_LARGEST_FIRST) {
			for (int projectileType : Projectile.TYPES) {
				EntityPool shipPool = victim.mEntities[shipType];
				EntityPool projectilePool = mEntities[projectileType];
				
				boolean flip = (shipPool.size() > projectilePool.size());
				
				sCH.initialize(victim, shipPool, projectilePool, flip);
				
				if (flip) {
					projectilePool.collide(shipPool, sCH);
				}
				else {
					shipPool.collide(projectilePool, sCH);
				}
				
				/*
			         		for (int type : Projectile.TYPES) {
			for (Entity entity : mEntities[type]) {
				
				Projectile projectile = (Projectile) entity;
				
				if (projectile.health >= 0) {
					// This projectile may be dead (from a previous attack),
					// in which case it will be removed later.
					projectile.attack(victim);
				}
				*/
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
			
			// Also, building multiple ships per frame makes it very easy for
			// the game to slow to a crawl if a player has lots of production
			// upgrades. Using "if" instead of "while" is a cheap form of
			// throttling. =)
			
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
				GameSounds.play(GameSounds.Sound.UPGRADE);
				mNumProductionSteps++;
				break;
		}
		
		if (mIsAI) {
			// This triggers the AI to determine a new build target. 
			mBuildTarget = BuildTarget.NONE;
		}
	}
	
	private Ship addShip(int type) {
		
		float spawnX = 0;
		float spawnY = 0;
		float heading = 0;
		
		boolean createShip = true;
		
		// Determine the location and heading of the new ship.
		if (type != Entity.FACTORY) {
			// If the ship being spawned ISN'T a factory, make it spawn at the
			// front of the factory.

			Ship factory = (Ship) mEntities[Entity.FACTORY].get(0);
			
			// This ship can only be built from a factory, and it's possible
			// that the player's factory has been destroyed.
			if (factory != null) {
				spawnX = factory.body.center.x + (float) (Factory.DIAMETER * 0.4 * Math.cos(factory.heading));
				spawnY = factory.body.center.y + (float) (Factory.DIAMETER * 0.4 * Math.sin(factory.heading));
				heading = factory.heading;
			}
			else {
				createShip = false;
			}
		}
		else {
			// If the ship being spawned IS a factory, make it spawn on the
			// perimeter of a circle.
			
			// The larger this value, the faster the factories will converge.
			float offset = (float) Math.PI / 40; 
			
			// The factory's initial distance from the map's center.
			float orbitRadius = GameRenderer.GAME_VIEW_SIZE / 4;
			
			// Distance in radians between factories.
			float spacing = (float) (2 * Math.PI / totalPlayers);
			
			// The angle in radians at which this particular factory will be spawned.
			float theta = (float) (spacing * playerNo - Math.PI / 2);
			
			spawnX = (float) (orbitRadius * Math.cos(theta));
			spawnY = (float) (orbitRadius * Math.sin(theta));
			heading = theta - (float) Math.PI / 2 - offset;
		}
		
		Ship ship = null;
		
		if (createShip) {
			ship = (Ship) mEntities[type].add(type, null);
			ship.body.center.set(spawnX, spawnY);
			ship.heading = heading;
		}
		
		return ship;
	}
	
	private Projectile addProjectile(Ship parent) {
		
		int projectileType;
		switch (parent.type) {
			case Entity.FIGHTER:
				GameSounds.play(GameSounds.Sound.SHOOT_LASER);
				projectileType = Entity.LASER;
				break;
			case Entity.BOMBER:
				GameSounds.play(GameSounds.Sound.SHOOT_BOMB);
				projectileType = Entity.BOMB;
				break;
			default:
			case Entity.FRIGATE:
				GameSounds.play(GameSounds.Sound.SHOOT_MISSILE);
				projectileType = Entity.MISSILE;
				break;
		}
		
		Projectile projectile = (Projectile) mEntities[projectileType].add(projectileType, parent);
		
		return projectile;
	}
	
	public EntityPool[] mEntities;
	public Emitter[] mEmitters;
	
	// Iterators require allocations, which can be expensive if they kick off
	// the garbage collector, so we use a custom collection instead for the
	// retarget and shooter queues. (They're not even queues, really. I should
	// probably rename them.)
	//
	// The retarget vector contains entities that want a new target. This queue
	// should be handled and cleared on every call to Game.update().
	public EntityVector mRetargetQueue;
	public EntityVector mShooterQueue;
	
	public float money;
	public BuildTarget mBuildTarget;
	public final int playerNo;
	public final int totalPlayers;
}
