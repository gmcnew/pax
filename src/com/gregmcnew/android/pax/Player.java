package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.List;

import com.gregmcnew.android.pax.Game.BuildTarget;

public class Player {
	
	
	// Public methods
	
	public Player() {
		 buildTarget = Game.BuildTarget.NONE;
		 quadtree = new Quadtree();
		 money = 0;
		 production = 0.75f;
		 production *= 30; // warp speed!
		 mShips = new ArrayList<Ship>();
		 addShip(new Factory(getID()));
	}
	
	public void produce() {
		money += production;
	}
	
	public void moveShips() {
		for (Ship ship : mShips) {
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
			int cost = Game.UnitCosts[buildTarget.ordinal()]; 
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
	
	private void addShip(Ship ship) {
		
		// Fix the ship's location. TODO: Use the factory's location.
		ship.location.set((float) Math.random() * 320, (float) Math.random() * 480);
		mShips.add(ship);
		quadtree.add(ship.id, new CircleF(ship.location, ship.radius));
	}
	
	private int getID() {
		return nextID++;
	}
	
	
	private int nextID = 0;
	
	public List<Ship> mShips;
	
	public float money;
	public float production;
	public Quadtree quadtree;
	public Game.BuildTarget buildTarget;
}
