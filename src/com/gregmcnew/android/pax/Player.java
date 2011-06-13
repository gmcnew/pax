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
		 money = 0;
		 production = 0.75f;
		 production *= 5; // warp speed!
		 mShipLists = new ArrayList<HolyArrayList<Ship>>();
		 mProjectileLists = new ArrayList<HolyArrayList<Projectile>>();
			
		 mShipLayers = new ArrayList<Quadtree>();
		
		 for (@SuppressWarnings("unused") int shipType : Ship.TYPES) {
			 mShipLists.add(new HolyArrayList<Ship>());
			 mShipLayers.add(new Quadtree());
		 }
		 
		 for (@SuppressWarnings("unused") int projectileType : Projectile.TYPES) {
			 mProjectileLists.add(new HolyArrayList<Projectile>());
		 }
		 
		 playerNo = playerNumber;
		 totalPlayers = players;
		 reset();
	}
	
	/**Removes all of a player's ships and projectiles and generates a new factory for that player.**/
	public void reset() {
		for (HolyArrayList<Ship> list : mShipLists) {
			list.clear();
		}
		for (HolyArrayList<Projectile> list : mProjectileLists) {
			list.clear();
		}
		
		addShip(Ship.Type.FACTORY);
	}
	
	public boolean hasLost() {
		return mShipLists.get(Ship.FACTORY).isEmpty();
	}
	
	public void produce() {
		money += production;
	}
	
	public void updateEntities() {
		
		for (int shipType : Ship.TYPES) {
			for (Ship ship : mShipLists.get(shipType)) {
			
				if (ship.health <= 0) {
					removeShip(ship);
				}
				else {
					float dv_x = (float)Math.cos(ship.heading) * ship.acceleration * Pax.UPDATE_INTERVAL_MS / 1000;
					float dv_y = (float)Math.sin(ship.heading) * ship.acceleration * Pax.UPDATE_INTERVAL_MS / 1000;
					
					ship.velocity.offset(dv_x, dv_y);
					if (ship.getSpeed() > ship.maxSpeed) {
						ship.fullSpeedAhead();
					}
					
					float dx_t = ship.velocity.x * Pax.UPDATE_INTERVAL_MS / 1000;
					float dy_t = ship.velocity.y * Pax.UPDATE_INTERVAL_MS / 1000;
					
					ship.body.center.offset(dx_t, dy_t);
					
					mShipLayers.get(shipType).update(ship.id);
					ship.updateHeading();
					
					if (ship.canShoot()) {
						addProjectile(ship);
					}
				}
			}
		}

		for (int projectileType : Projectile.TYPES) {
			for (Projectile projectile : mProjectileLists.get(projectileType)) {
				
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
	}
	
	public void attack(Player victim) {
		for (int projectileType : Projectile.TYPES) {
			for (Projectile projectile : mProjectileLists.get(projectileType)) {
				projectile.attack(victim);
				
				if (projectile.health <= 0) {
					removeProjectile(projectile);
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
				ship = new Fighter();
				break;
			case BOMBER:
				ship = new Bomber();
				break;
			case FRIGATE:
				ship = new Frigate();
				break;
			case FACTORY:
				ship = new Factory();
				break;
		}
		
		if (ship != null) {
			int shipType = type.ordinal() - Entity.MIN_SHIP_TYPE;
			id = mShipLists.get(shipType).add(ship);
			ship.id = id;
			
			// Fix the ship's location.
			if (type != Ship.Type.FACTORY){ // If the ship being spawned ISN'T a factory...
				Ship factory = mShipLists.get(Ship.FACTORY).get(0);
				float spawnX, spawnY;
				spawnX = factory.body.center.x + (float) (55 * Math.cos(factory.heading));
				spawnY = factory.body.center.y + (float) (55 * Math.sin(factory.heading));
				ship.body.center.set(spawnX, spawnY);
				ship.heading = factory.heading;
			}
			else{ // If the ship being spawned IS a factory...
				float factoryX = 0, factoryY = 0;
				float offset = (float) Math.PI/40; // The larger this value, the faster the factories will converge.
		    	
				Display display = ((WindowManager) Pax.thisContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				PointF screenSize = new PointF(display.getWidth(), display.getHeight()); // Stores the screen size in a point.
				
				float orbitRadius = screenSize.x*1/3; // The radius that the factory will orbit the center at.
				float spacing = (float)(2*Math.PI / totalPlayers);// The spacing in radians between the factories.
				float theta = spacing*(float)(-.5 + playerNo);// The angle in radians at which this particular factory will be spawned.
				
				factoryX = screenSize.x/2 + (float) (orbitRadius * Math.cos(theta));
				factoryY = screenSize.y/2 + (float) (orbitRadius * Math.sin(theta));
				
				ship.body.center.set(factoryX, factoryY);
				ship.heading = theta - (float) Math.PI/2 - offset;
			}
			mShipLayers.get(shipType).add(ship.id, ship.body);
		}
		
		return id;
	}
	
	private int addProjectile(Ship parent) {
		
		int id = Game.NO_ENTITY;
		
		Projectile projectile = null;
		switch (parent.type) {
			case FIGHTER:
				projectile = new Laser(parent);
				break;
			case BOMBER:
				projectile = new Bomb(parent);
				break;
			case FRIGATE:
				projectile = new Missile(parent);
				break;
		}
		
		if (projectile != null) {
			id = mProjectileLists.get(projectile.type.ordinal() - Entity.MIN_PROJECTILE_TYPE).add(projectile);
			projectile.id = id;
		}
		
		return id;
	}
	
	private void removeShip(Ship ship) {
		int id = ship.id;
		int shipType = ship.type.ordinal() - Entity.MIN_SHIP_TYPE;
		mShipLayers.get(shipType).remove(id);
		mShipLists.get(shipType).remove(id);
	}
	
	private void removeProjectile(Projectile projectile) {
		int id = projectile.id;
		int projectileType = projectile.type.ordinal() - Entity.MIN_PROJECTILE_TYPE;
		mProjectileLists.get(projectileType).remove(id);
	}
	
	public List<HolyArrayList<Ship>> mShipLists;
	public List<HolyArrayList<Projectile>> mProjectileLists;
	
	public List<Quadtree> mShipLayers;
	
	public float money;
	public float production;
	public BuildTarget buildTarget;
	public int playerNo;
	public int totalPlayers;
}
