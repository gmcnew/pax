package com.gregmcnew.android.pax;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import android.content.Context;
import android.graphics.PointF;
import android.view.Display;
import android.view.WindowManager;

public class Player {

	public enum BuildTarget { FIGHTER, BOMBER, FRIGATE, UPGRADE, NONE }
	public static int[] BuildCosts = { 50, 170, 360, 1080, 0 };
	
	
	// Public methods
	
	public Player(int playerNumber, int players) {
		 buildTarget = BuildTarget.NONE;
		 money = 0;
		 production = 0.75f;
		 production *= 5; // warp speed!
		 
		 mEntities = new EnumMap<Entity.Type, HolyArrayList<Entity>>(Entity.Type.class);
		 mBodies = new EnumMap<Entity.Type, Quadtree>(Entity.Type.class);
		 mRetargetQueue = new LinkedList<Entity>();
		 
		 for (Entity.Type type : Entity.Type.values()) {
			 mEntities.put(type, new HolyArrayList<Entity>());
			 mBodies.put(type, new Quadtree());
		 }
		 
		 playerNo = playerNumber;
		 totalPlayers = players;
		 reset();
	}
	
	/**Removes all of a player's ships and projectiles and generates a new factory for that player.**/
	public void reset() {
		for (Entity.Type type : Entity.Type.values()) {
			mEntities.get(type).clear();
			mBodies.get(type).clear();
		}
		
		mRetargetQueue.clear();
		
		addShip(Ship.Type.FACTORY);
	}
	
	public boolean hasLost() {
		return mEntities.get(Entity.Type.FACTORY).isEmpty();
	}
	
	public void produce() {
		money += production;
	}
	
	public void updateEntities() {
		
		for (Entity.Type type : Entity.Type.values()) {
			for (Entity entity : mEntities.get(type)) {
				
				if (entity.wantsNewTarget()) {
					mRetargetQueue.add(entity);
				}
				
				if (type == Entity.Type.FIGHTER || type == Entity.Type.BOMBER || type == Entity.Type.FRIGATE || type == Entity.Type.FACTORY) {
				
					Ship ship = (Ship) entity;
					if (ship.health <= 0) {
						removeEntity(ship);
					}
					else {
						ship.move();
						
						if (ship.canShoot()) {
							//addProjectile(ship);
						}
					}
				}
				else { // it's a projectile
					Projectile projectile = (Projectile) entity;
					
					if (projectile.health <= 0 || projectile.lifeMs <= 0) {
						removeEntity(projectile);
					}
					else {
						projectile.move();
						projectile.lifeMs -= Pax.UPDATE_INTERVAL_MS;
					}
				}
			}
		}
	}
	
	public void attack(Player victim) {
		for (Entity.Type projectileType : Projectile.TYPES) {
			for (Entity entity : mEntities.get(projectileType)) {
				Projectile projectile = (Projectile) entity;
				
				projectile.attack(victim);
				
				if (projectile.health <= 0) {
					removeEntity(projectile);
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
	
	private int addShip(Entity.Type type) {
		
		int id = Entity.NO_ENTITY;
			
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
			id = mEntities.get(type).add(ship);
			ship.id = id;
			
			// Fix the ship's location.
			if (type != Ship.Type.FACTORY){ // If the ship being spawned ISN'T a factory...
				Ship factory = (Ship) mEntities.get(Entity.Type.FACTORY).get(0);
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
			mBodies.get(type).add(ship.id, ship.body);
		}
		
		return id;
	}
	
	private int addProjectile(Ship parent) {
		
		int id = Entity.NO_ENTITY;
		
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
			id = mEntities.get(projectile.type).add(projectile);
			projectile.id = id;
		}
		
		return id;
	}
	
	private void removeEntity(Entity entity) {
		int id = entity.id;
		mEntities.get(entity.type).remove(id);
		mBodies.get(entity.type).remove(id);
	}
	
	public Map<Entity.Type, HolyArrayList<Entity>> mEntities;
	
	public Map<Entity.Type, Quadtree> mBodies;
	
	// The retarget queue contains entities that want a new target. This queue
	// should be handled and cleared on every call to Game.update().
	public Queue<Entity> mRetargetQueue;
	
	public float money;
	public float production;
	public BuildTarget buildTarget;
	public int playerNo;
	public int totalPlayers;
}
