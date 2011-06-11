package com.gregmcnew.android.pax;

import java.util.HashMap;

import android.graphics.RectF;

public class Quadtree {
	
	public Quadtree() {
		bounds = null;
		mCircs = new HashMap<Integer, CircleF>();
	}
	
	/*
	public Quadtree(float width, float height) {
		bounds = new RectF(0, 0, width, height);
		mCircs = new HashMap<Integer, CircleF>();
	}
	*/
	
	public boolean add(int id, CircleF circ) {
		if (id != Game.NO_ENTITY && (bounds == null || bounds.contains(circ.center.x, circ.center.y))) {
			mCircs.put(id, circ);
			return true;
		}
		return false;
	}
	
	public boolean remove(int id) {
		return mCircs.remove(id) != null;
	}
	
	// See if a given circle intersects with anything in the quadtree.
	// If so, return the ID of the circle with the greatest overlap.
	public int collide(CircleF circ) {
		float maxOverlap = 0;
		int id = Game.NO_ENTITY;
		
		for (HashMap.Entry<Integer, CircleF> entry : mCircs.entrySet()) {
		    CircleF circ2 = entry.getValue();
		    float radiuses = circ.radius + circ2.radius;
		    float overlap = (radiuses * radiuses) - circ.distanceToSquared(circ2);
		    if (overlap > maxOverlap) {
		    	maxOverlap = overlap;
		    	id = entry.getKey();
		    }
		}
		
		return id;
	}

	private HashMap<Integer, CircleF> mCircs;
	public final RectF bounds;
}
