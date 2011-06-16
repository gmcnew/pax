package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EntityPool implements Iterable<Entity> {

	public EntityPool(int type) {
		mList = new ArrayList<Entity>();
		mRecycledIDs = new HashSet<Integer>();
		
		// TODO: Allow a quadtree to grow to more than 1024 points if necessary.
		mBodies = new Quadtree(Quadtree.X, Entity.Radii[type], new Point2[1024]);
	}
	
	public int add(Entity e) {
		int id;
		if (mRecycledIDs.isEmpty()) {
			id = mList.size();
			mList.add(e);
		}
		else {
			id = mRecycledIDs.iterator().next();
			mRecycledIDs.remove(id);
			mList.set(id, e);
		}
		
		e.id = id;
		e.body.center.id = id;
		
		return id;
	}
	
	public Entity get(int id) {
		return mList.get(id);
	}
	
	public int size() {
		return mList.size() - mRecycledIDs.size();
	}
	
	public boolean isEmpty() {
		return size() <= 0;
	}
	
	public void clear() {
		mList.clear();
		mRecycledIDs.clear();
		mBodies.reset(0, 0);
	}

	public Iterator<Entity> iterator() {
		return new EntityPoolIterator(this, mList);
	}
	
	public Entity collide(Point2 center, float radius) {
		Entity entity = null;
		Point2 p = mBodies.collide(center, radius);
		if (p != null) {
			entity = get(p.id);
		}
		return entity;
	}
	
	public void invalidateCollisionSpaces() {
		mBodies.invalidate();
	}
	
	public void rebuildCollisionSpaces() {

		// Tweak all of the points in the quadtree, then reset it.
		int i = 0;
		for (Entity e : this) {
			mBodies.mPoints[i] = e.body.center;
			i++;
		}
		mBodies.reset(0, i);
	}
	
	public void remove(int id) {
		assert(id != Entity.NO_ENTITY);
		remove(mList.get(id));
	}
	
	public void remove(Entity entity) {

		assert(entity.id != Entity.NO_ENTITY);
		assert(entity.id == entity.body.center.id);
		
		mList.set(entity.id, null);
		mRecycledIDs.add(entity.id);
		mBodies.remove(entity.body.center);
		
		entity.id = Entity.NO_ENTITY;
		entity.body.center.id = Entity.NO_ENTITY;
	}

	// Use a set to eliminate duplicates and ensure that an ID can't be recycled twice.
	private List<Entity> mList;
	private Set<Integer> mRecycledIDs;
	
	protected Quadtree mBodies;
	
	private static final long serialVersionUID = 9001749495484315418L;
}
