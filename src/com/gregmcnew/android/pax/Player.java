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
		 mEntities = new ArrayList<Entity>();
		 ids = new IDPool();
		 
		 addEntity(new Factory(ids.get()));
	}
	
	public void produce() {
		money += production;
	}
	
	public void updateEntities() {
		
		List<Entity> shootQueue = new ArrayList<Entity>();
		for (Entity entity : mEntities) {
			if (entity == null) {
				continue;
			}
			
			if (entity.health <= 0) {
				removeEntity(entity.id);
			}
			else {
				float ax = (float) Math.random() - 0.5f;
				float ay = (float) Math.random() - 0.5f;
				entity.velocity.offset(ax * entity.acceleration, ay * entity.acceleration);
				entity.location.offset(entity.velocity.x, entity.velocity.y);
				if (entity.isShip) { 
					shipBodies.remove(entity.id);
					shipBodies.add(entity.id, new CircleF(entity.location, entity.radius));
					
					if (entity.canShoot()) {
						shootQueue.add(entity);
					}
				}
			}
		}
		
		for (Entity entity : shootQueue) {
			addProjectile(entity);
		}
	}
	
	public void addProjectile(Entity parent) {
		Entity projectile = null;
		switch (parent.type) {
			case FIGHTER:
				projectile = new Laser(ids.get(), parent);
		}
		
		if (projectile != null) {
			addEntity(projectile);
		}
	}
	
	public void tryToKill(List<Player> allPlayers) {
		for (Player player : allPlayers) {

			// We're in the list, but we shouldn't try to kill ourselves.
			if (player == this) {
				continue;
			}
			
			Player otherPlayer = player;
			
			for (Entity entity : mEntities) {
				if (entity != null && !entity.isShip) {
				
					Entity projectile = entity;
					
					int id = otherPlayer.shipBodies.collide(projectile.location.x, projectile.location.y, projectile.radius);
					if (id != Game.NO_ENTITY) {
						Entity target = otherPlayer.mEntities.get(id);
						
						target.health -= projectile.health;
						
						// Kill the projectile.
						removeEntity(projectile.id);
						
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
				addEntity(new Fighter(ids.get()));
				break;
			case BOMBER:
				addEntity(new Bomber(ids.get()));
				break;
			case FRIGATE:
				addEntity(new Frigate(ids.get()));
				break;
			case UPGRADE:
				production += 0.25f;
				break;
		}
	}
	
	private int addEntity(Entity entity) {
		
		if (entity.id < mEntities.size()) {
			mEntities.set(entity.id, entity);
		}
		else {
			mEntities.add(entity);
		}
		
		if (entity.isShip) {
			// Fix the ship's location. TODO: Use the factory's location.
			entity.location.set((float) Math.random() * 320, (float) Math.random() * 480);
			
			shipBodies.add(entity.id, new CircleF(entity.location, entity.radius));
		}
		
		return entity.id;
	}
	
	/*
	 * Returns true if the entity was removed.
	 */
	public boolean removeEntity(int id) {
		if (id < mEntities.size())
		{
			shipBodies.remove(id);
			mEntities.set(id, null);
			ids.recycle(id);
			return true;
		}
		return false;
	}

	public List<Entity> mEntities;
	private IDPool ids;
	
	public float money;
	public float production;
	public Quadtree shipBodies;
	public BuildTarget buildTarget;
}
