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
		 mProjectiles = new ArrayList<Projectile>();
		 shipIDs = new IDPool();
		 projectileIDs = new IDPool();
		 reset();
	}
	
	/**Removes all of a player's ships and projectiles and generates a new factory for that player.**/
	public void reset() {
		for (Ship ship : mShips) {
			if (ship != null) {
				removeShip(ship);
			}
		}
		
		for (Projectile projectile : mProjectiles) {
			if (projectile != null) {
				removeProjectile(projectile);
			}
		}
		
		addShip(Ship.Type.FACTORY);
	}
	
	public boolean hasLost() {
		// The factory will always have ID 0, since it's the first ship to be
		// created.
		Ship shipZero = mShips.get(0);
		return (shipZero == null || shipZero.type != Entity.Type.FACTORY);
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
				float dv_x = ax * ship.acceleration;
				float dv_y = ay * ship.acceleration;
				ship.velocity.offset(dv_x, dv_y);
				if (ship.getSpeed() > ship.maxSpeed) {
					ship.fullSpeedAhead();
				}
				ship.body.center.offset(ship.velocity.x, ship.velocity.y);
				shipBodies.update(ship.id);
				
				if (ship.canShoot()) {
					addProjectile(ship);
				}
			}
		}
		
		for (Projectile projectile : mProjectiles) {
			if (projectile == null) {
				continue;
			}
			
			if (projectile.health <= 0 || projectile.lifeMs <= 0) {
				removeProjectile(projectile);
			}
			else {
				float ax = (float) Math.random() - 0.5f;
				float ay = (float) Math.random() - 0.5f;				
				float dv_x = ax * projectile.acceleration;
				float dv_y = ay * projectile.acceleration;
				projectile.velocity.offset(dv_x, dv_y);
				if (projectile.getSpeed() > projectile.maxSpeed) {
					projectile.fullSpeedAhead();
				}
				projectile.body.center.offset(projectile.velocity.x, projectile.velocity.y);
				projectile.lifeMs -= Pax.UPDATE_INTERVAL_MS;
			}
		}
	}
	
	public void attack(Player victim) {
		for (Projectile projectile : mProjectiles) {
			if (projectile != null) {
				
				int id = victim.shipBodies.collide(projectile.body.center.x, projectile.body.center.y, projectile.body.radius);
				if (id != Game.NO_ENTITY) {
					Ship target = victim.mShips.get(id);
					
					int damage = projectile.health;
					
					// XXX: Make projectiles superpowered!
					damage *= 100;
					
					target.health -= damage;
					
					// Kill the projectile.
					removeProjectile(projectile);
					
					if (target.health <= 0) {
						// Go ahead and remove the target from shipBodies
						// so it doesn't block other projectiles.
						// Its ID won't be recycled until later, when
						// Player.updateEntities() is called.
						victim.shipBodies.remove(id);
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
				addShip(Ship.Type.FIGHTER);
				break;
			case BOMBER:
				addShip(Ship.Type.BOMBER);
				break;
			case FRIGATE:
				addShip(Ship.Type.FRIGATE);
				break;
			case UPGRADE:
				production += 0.25f;
				break;
		}
	}
	
	private int addShip(Ship.Type type) {
		
		int id = Game.NO_ENTITY;
			
		Ship ship = null;
		switch (type) {
			case FIGHTER:
				ship = new Fighter(shipIDs.get());
				break;
			case BOMBER:
				ship = new Bomber(shipIDs.get());
				break;
			case FRIGATE:
				ship = new Frigate(shipIDs.get());
				break;
			case FACTORY:
				ship = new Factory(shipIDs.get());
				break;
		}
		
		if (ship != null) {
			id = ship.id;
			
			// The ships array will need to grow if this isn't a recycled ID.
			if (id == mShips.size()) {
				mShips.add(null);
			}
			mShips.set(id, ship);
			
			// Fix the ship's location. TODO: Use the factory's location.
			ship.body.center.set((float) Math.random() * 320, (float) Math.random() * 480);
			
			shipBodies.add(ship.id, ship.body);
		}
		
		return id;
	}
	
	private int addProjectile(Ship parent) {
		
		int id = Game.NO_ENTITY;
		
		Projectile projectile = null;
		switch (parent.type) {
			case FIGHTER:
				projectile = new Laser(projectileIDs.get(), parent);
				break;
			// TODO: Add bombs for bombers and missiles for frigates.
		}
		
		if (projectile != null) {
			id = projectile.id;
			
			// The projectiles array will need to grow if this isn't a recycled ID.
			if (id == mProjectiles.size()) {
				mProjectiles.add(null);
			}
			mProjectiles.set(id, projectile);
		}
		
		return id;
	}
	
	private void removeShip(Ship ship) {
		int id = ship.id;
		shipBodies.remove(id);
		mShips.set(id, null);
		shipIDs.recycle(id);
	}
	
	private void removeProjectile(Projectile projectile) {
		int id = projectile.id;
		mProjectiles.set(id, null);
		projectileIDs.recycle(id);
	}

	public List<Ship> mShips;
	public List<Projectile> mProjectiles;
	private IDPool shipIDs;
	private IDPool projectileIDs;
	
	public float money;
	public float production;
	public Quadtree shipBodies;
	public BuildTarget buildTarget;
}
