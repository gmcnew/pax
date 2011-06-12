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
				ship.velocity.offset(ax * ship.acceleration, ay * ship.acceleration);
				shipBodies.remove(ship.id);
				ship.body.center.offset(ship.velocity.x, ship.velocity.y);
				shipBodies.add(ship.id, ship.body);
				
				if (ship.canShoot()) {
					addProjectile(ship);
				}
			}
		}
		
		for (Projectile projectile : mProjectiles) {
			if (projectile == null) {
				continue;
			}
			
			if (projectile.health <= 0 || projectile.lifeMs < 0) {
				removeProjectile(projectile);
			}
			else {
				float ax = (float) Math.random() - 0.5f;
				float ay = (float) Math.random() - 0.5f;
				projectile.velocity.offset(ax * projectile.acceleration, ay * projectile.acceleration);
				projectile.body.center.offset(projectile.velocity.x, projectile.velocity.y);
				projectile.lifeMs -= Pax.UPDATE_INTERVAL_MS;
			}
		}
	}
	
	private int addProjectile(Ship parent) {
		
		int id = projectileIDs.get();
		
		Projectile projectile = null;
		switch (parent.type) {
			case FIGHTER:
				projectile = new Laser(id, parent);
				break;
		}
		
		if (projectile == null) {
			projectileIDs.recycle(id);
			id = Game.NO_ENTITY;
		}
		else {
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
			
			for (Projectile projectile : mProjectiles) {
				if (projectile != null) {
					
					int id = otherPlayer.shipBodies.collide(projectile.body.center.x, projectile.body.center.y, projectile.body.radius);
					if (id != Game.NO_ENTITY) {
						Ship target = otherPlayer.mShips.get(id);
						
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
			if (id < mShips.size()) {
				mShips.set(id, ship);
			}
			else {
				mShips.add(ship);
			}
			
			// Fix the ship's location. TODO: Use the factory's location.
			ship.body.center.set((float) Math.random() * 320, (float) Math.random() * 480);
			
			shipBodies.add(ship.id, ship.body);
		}
		
		return id;
	}
	
	public void removeShip(Ship ship) {
		int id = ship.id;
		shipBodies.remove(id);
		mShips.set(id, null);
		shipIDs.recycle(id);
	}
	
	public void removeProjectile(Projectile projectile) {
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
