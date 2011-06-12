package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
		 recycledIDs = new Stack<Integer>();
		 
		 addEntity(new Factory(getID()));
	}
	
	public void produce() {
		money += production;
	}
	
	public void moveShips() {
		for (Entity entity : mEntities) {
			if (entity == null) {
				continue;
			}
			
			float ax = (float) Math.random() - 0.5f;
			float ay = (float) Math.random() - 0.5f;
			entity.velocity.offset(ax * entity.acceleration, ay * entity.acceleration);
			entity.location.offset(entity.velocity.x, entity.velocity.y);
			shipBodies.remove(entity.id);
			shipBodies.add(entity.id, new CircleF(entity.location, entity.radius));
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
				addEntity(new Fighter(getID()));
				break;
			case BOMBER:
				addEntity(new Bomber(getID()));
				break;
			case FRIGATE:
				addEntity(new Frigate(getID()));
				break;
			case UPGRADE:
				production += 0.25f;
				break;
		}
	}
	
	private int addEntity(Entity entity) {
		
		// Fix the ship's location. TODO: Use the factory's location.
		entity.location.set((float) Math.random() * 320, (float) Math.random() * 480);
		if (entity.id < mEntities.size()) {
			mEntities.set(entity.id, entity);
		}
		else {
			mEntities.add(entity);
		}
		
		if (entity.isShip) {
			shipBodies.add(entity.id, new CircleF(entity.location, entity.radius));
		}
		
		return entity.id;
	}
	
	/*
	 * Returns true if the ship was removed.
	 */
	public boolean removeEntity(int id) {
		if (id < mEntities.size())
		{
			shipBodies.remove(id);
			mEntities.set(id, null);
			recycledIDs.add(id);
			return true;
		}
		return false;
	}
	
	private int getID() {
		int id;
		if (!recycledIDs.isEmpty()) {
			id = recycledIDs.pop();
		}
		else {
			id = nextID++;
		}
		return id;
	}

	public List<Entity> mEntities;
	private Stack<Integer> recycledIDs;
	private int nextID = 0;
	
	public float money;
	public float production;
	public Quadtree shipBodies;
	public BuildTarget buildTarget;
}
