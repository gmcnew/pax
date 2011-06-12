package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.List;

public class Player {

	public enum BuildTarget { FIGHTER, BOMBER, FRIGATE, UPGRADE, NONE };
	public static int[] BuildCosts = { 50, 170, 360, 1080, 0 };
	
	// Public methods
	
	public Player() {
		 buildTarget = BuildTarget.NONE;
		 shipBodies = new Quadtree();
		 money = 0;
		 production = 0.75f;
		 production *= 30; // warp speed!
		 mShips = new ArrayList<Ship>();
		 mProjectiles = new ArrayList<Entity>();
		 shipIDs = new IDPool();
		 projectileIDs = new IDPool();
		 
		 addShip(Entity.Type.FACTORY);
	}
	
	public void produce() {
		money += production;
	}
	
	public void updateEntities() {
		
		for (Ship ship : mShips) {
			if (ship == null) {
				continue;
			}
			
			if (ship.health <= 0) {
				removeShip(ship);
			}
			else {
				float ax = (float) Math.random() - 0.5f;
				float ay = (float) Math.random() - 0.5f;
				ship.velocity.offset(ax * ship.acceleration, ay * ship.acceleration);
				ship.location.offset(ship.velocity.x, ship.velocity.y);
				shipBodies.remove(ship.id);
				shipBodies.add(ship.id, new CircleF(ship.location, ship.radius));
				
				if (ship.canShoot()) {
					addProjectile(ship);
				}
			}
		}
		
		for (Entity projectile : mProjectiles) {
			if (projectile == null) {
				continue;
			}
			
			if (projectile.health <= 0) {
				removeProjectile(projectile);
			}
			else {
				float ax = (float) Math.random() - 0.5f;
				float ay = (float) Math.random() - 0.5f;
				projectile.velocity.offset(ax * projectile.acceleration, ay * projectile.acceleration);
				projectile.location.offset(projectile.velocity.x, projectile.velocity.y);
			}
		}
	}
	
	private int addProjectile(Entity parent) {
		
		int id = projectileIDs.get();
		
		Entity projectile = null;
		switch (parent.type) {
			case FIGHTER:
				projectile = new Laser(projectileIDs.get(), parent);
				break;
		}
		
		if (projectile == null) {
			projectileIDs.recycle(id);
			id = Game.NO_ENTITY;
		}
		else {
			assert(!projectile.isShip);
			
			if (id < mProjectiles.size()) {
				mProjectiles.set(id, projectile);
			}
			else {
				mProjectiles.add(projectile);
			}
		}
		
		return id;
	}
	
	public void tryToKill(List<Player> allPlayers) {
		for (Player player : allPlayers) {

			// We're in the list, but we shouldn't try to kill ourselves.
			if (player == this) {
				continue;
			}
			
			Player otherPlayer = player;
			
			for (Entity projectile : mProjectiles) {
				if (projectile != null) {
					
					int id = otherPlayer.shipBodies.collide(projectile.location.x, projectile.location.y, projectile.radius);
					if (id != Game.NO_ENTITY) {
						Ship target = otherPlayer.mShips.get(id);
						
						target.health -= projectile.health;
						
						// Kill the projectile.
						removeProjectile(projectile);
						
						if (target.health <= 0) {
							// Go ahead and remove the target from shipBodies
							// so it doesn't block other projectiles.
							// Its ID won't be recycled until later, when
							// Player.updateEntities() is called.
							otherPlayer.shipBodies.remove(id);
						}
					}
				}
			}
		}
	}
	
	public void build() {
		if (buildTarget != BuildTarget.NONE) {
			int cost = BuildCosts[buildTarget.ordinal()]; 
			if (money >= cost) {
				build(buildTarget);
				money -= cost;
			}
		}
	}
	
	
	// Private methods
	
	private void build(BuildTarget buildTarget) {
		switch (buildTarget) {
			case FIGHTER:
				addShip(Entity.Type.FIGHTER);
				break;
			case BOMBER:
				addShip(Entity.Type.BOMBER);
				break;
			case FRIGATE:
				addShip(Entity.Type.FRIGATE);
				break;
			case UPGRADE:
				production += 0.25f;
				break;
		}
	}
	
	private int addShip(Entity.Type type) {
		
		int id = shipIDs.get();
			
		Ship ship = null;
		switch (type) {
			case FIGHTER:
				ship = new Fighter(id);
				break;
			case BOMBER:
				ship = new Bomber(id);
				break;
			case FRIGATE:
				ship = new Frigate(id);
				break;
			case FACTORY:
				ship = new Factory(id);
				break;
		}
		
		if (ship == null) {
			shipIDs.recycle(id);
			id = Game.NO_ENTITY;
		}
		else {
			assert(ship.isShip);
			
			if (id < mShips.size()) {
				mShips.set(id, ship);
			}
			else {
				mShips.add(ship);
			}
			
			// Fix the ship's location. TODO: Use the factory's location.
			ship.location.set((float) Math.random() * 320, (float) Math.random() * 480);
			
			shipBodies.add(ship.id, new CircleF(ship.location, ship.radius));
		}
		
		return id;
	}
	
	/*
	 * Returns true if the ship was removed.
	 */
	public boolean removeShip(Ship ship) {
		assert(ship.isShip);
		int id = ship.id;
		if (id < mShips.size())
		{
			shipBodies.remove(id);
			mShips.set(id, null);
			shipIDs.recycle(id);
			return true;
		}
		return false;
	}
	
	/*
	 * Returns true if the projectile was removed.
	 */
	public boolean removeProjectile(Entity projectile) {
		assert(!projectile.isShip);
		int id = projectile.id;
		if (id < mProjectiles.size())
		{
			mProjectiles.set(id, null);
			projectileIDs.recycle(id);
			return true;
		}
		return false;
	}

	public List<Ship> mShips;
	public List<Entity> mProjectiles;
	private IDPool shipIDs;
	private IDPool projectileIDs;
	
	public float money;
	public float production;
	public Quadtree shipBodies;
	public BuildTarget buildTarget;
}
