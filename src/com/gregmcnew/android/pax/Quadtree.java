package com.gregmcnew.android.pax;

import java.util.HashMap;

import android.graphics.RectF;

// TODO: Make this an actual quadtree. =)

public class Quadtree {
	
	public static float NO_SEARCH_LIMIT = -1;
	
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
		if (id != Entity.NO_ENTITY && (bounds == null || bounds.contains(circ.center.x, circ.center.y))) {
			mCircs.put(id, circ);
			return true;
		}
		return false;
	}
	
	public boolean remove(int id) {
		return mCircs.remove(id) != null;
	}
	
	public void update(int id) {
		// In an actual quadtree, this function would do stuff. =)
	}
	
	public void clear() {
		mCircs.clear();
	}
	
	public int collide(CircleF circ) {
		return collide(circ.center.x, circ.center.y, circ.radius);
	}
	
	// See if a given circle intersects with anything in the quadtree.
	// If so, return the ID of the circle with the greatest overlap.
	public int collide(float centerX, float centerY, float radius) {
		float maxOverlap = 0;
		int id = Entity.NO_ENTITY;
		
		for (HashMap.Entry<Integer, CircleF> entry : mCircs.entrySet()) {
		    CircleF circ2 = entry.getValue();
		    float radiuses = radius + circ2.radius;
		    float overlap = (radiuses * radiuses) - distanceSquared(circ2.center.x, circ2.center.y, centerX, centerY);
		    if (overlap > maxOverlap) {
		    	maxOverlap = overlap;
		    	id = entry.getKey();
		    }
		}
		
		return id;
	}
	
	private float distanceSquared(float x1, float y1, float x2, float y2) {
		float dx = x1 - x2;
		float dy = y1 - y2;
		return dx * dx + dy * dy;
	}

	private HashMap<Integer, CircleF> mCircs;
	public final RectF bounds;
}
