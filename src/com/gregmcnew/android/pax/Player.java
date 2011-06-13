package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.PointF;
import android.view.Display;
import android.view.WindowManager;

public class Player {

	public enum BuildTarget { FIGHTER, BOMBER, FRIGATE, UPGRADE, NONE };
	public static int[] BuildCosts = { 50, 170, 360, 1080, 0 };
	
	// Public methods
	
	public Player(int playerNumber, int players) {
		 buildTarget = BuildTarget.NONE;
		 shipBodies = new Quadtree();
		 money = 0;
		 production = 0.75f;
		 production *= 30; // warp speed!
		 mShips = new ArrayList<Ship>();
		 mProjectiles = new ArrayList<Projectile>();
		 shipIDs = new IDPool();
		 projectileIDs = new IDPool();
		 playerNo = playerNumber;
		 totalPlayers = players;
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
				float dv_x = ax * ship.acceleration * Pax.UPDATE_INTERVAL_MS / 1000;
				float dv_y = ay * ship.acceleration * Pax.UPDATE_INTERVAL_MS / 1000;
				ship.velocity.offset(dv_x, dv_y);
				if (ship.getSpeed() > ship.maxSpeed) {
					ship.fullSpeedAhead();
				}
				float dx_t = ship.velocity.x * Pax.UPDATE_INTERVAL_MS / 1000;
				float dy_t = ship.velocity.y * Pax.UPDATE_INTERVAL_MS / 1000;
				ship.body.center.offset(dx_t, dy_t);
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
				float dv_x = ax * projectile.acceleration * Pax.UPDATE_INTERVAL_MS / 1000;
				float dv_y = ay * projectile.acceleration * Pax.UPDATE_INTERVAL_MS / 1000;
				projectile.velocity.offset(dv_x, dv_y);
				if (projectile.getSpeed() > projectile.maxSpeed) {
					projectile.fullSpeedAhead();
				}
				float dx_t = projectile.velocity.x * Pax.UPDATE_INTERVAL_MS / 1000;
				float dy_t = projectile.velocity.y * Pax.UPDATE_INTERVAL_MS / 1000;
				projectile.body.center.offset(dx_t, dy_t);
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
			
			// Fix the ship's location.
			if (id != 0){
				Ship factory = mShips.get(0);
				float spawnX, spawnY;
				spawnX = factory.body.center.x + (float) (60 * Math.cos(factory.heading));
				spawnY = factory.body.center.y + (float) (60 * Math.sin(factory.heading));
				ship.body.center.set(spawnX, spawnY);
				ship.heading = factory.heading;
			}
			else{
				float factoryX = 0, factoryY = 0;
		    	Display display = ((WindowManager) Pax.thisContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				PointF screenSize = new PointF(display.getWidth(), display.getHeight());
				float orbitRadius = screenSize.x*1/3; // The radius that the factory will orbit the center at.
				float spacing = (float)(2*Math.PI / totalPlayers);// The spacing in radians between the factories.
				float theta = spacing*(float)(-.5 + playerNo);// The angle in radians at which this particular factory will be spawned.
				factoryX = screenSize.x/2 + (float) (orbitRadius * Math.cos(theta));
				factoryY = screenSize.y/2 + (float) (orbitRadius * Math.sin(theta));
				ship.body.center.set(factoryX, factoryY);
				ship.heading = theta - (float) Math.PI/2;
			}
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
			case BOMBER:
				projectile = new Bomb(projectileIDs.get(), parent);
				break;
			case FRIGATE:
				projectile = new Missile(projectileIDs.get(), parent);
				break;
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
	public int playerNo;
	public int totalPlayers;
}
