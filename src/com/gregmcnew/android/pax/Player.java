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
		 quadtree = new Quadtree();
		 money = 0;
		 production = 0.75f;
		 production *= 30; // warp speed!
		 mShips = new ArrayList<Ship>();
		 recycledIDs = new Stack<Integer>();
		 
		 addShip(new Factory(getID()));
	}
	
	public void produce() {
		money += production;
	}
	
	public void moveShips() {
		for (Ship ship : mShips) {
			if (ship == null) {
				continue;
			}
			
			float ax = (float) Math.random() - 0.5f;
			float ay = (float) Math.random() - 0.5f;
			ship.velocity.offset(ax * ship.acceleration, ay * ship.acceleration);
			ship.location.offset(ship.velocity.x, ship.velocity.y);
			quadtree.remove(ship.id);
			quadtree.add(ship.id, new CircleF(ship.location, ship.radius));
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
				addShip(new Fighter(getID()));
				break;
			case BOMBER:
				addShip(new Bomber(getID()));
				break;
			case FRIGATE:
				addShip(new Frigate(getID()));
				break;
			case UPGRADE:
				production += 0.25f;
				break;
		}
	}
	
	private int addShip(Ship ship) {
		
		// Fix the ship's location. TODO: Use the factory's location.
		ship.location.set((float) Math.random() * 320, (float) Math.random() * 480);
		if (ship.id < mShips.size()) {
			mShips.set(ship.id, ship);
		}
		else {
			mShips.add(ship);
		}
		quadtree.add(ship.id, new CircleF(ship.location, ship.radius));
		
		return ship.id;
	}
	
	/*
	 * Returns true if the ship was removed.
	 */
	public boolean removeShip(int id) {
		if (id < mShips.size())
		{
			quadtree.remove(id);
			mShips.set(id, null);
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
	
	private Stack<Integer> recycledIDs;
	private int nextID = 0;
	
	public List<Ship> mShips;
	
	public float money;
	public float production;
	public Quadtree quadtree;
	public BuildTarget buildTarget;
}
