package com.gregmcnew.android.pax;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EntityPool implements Iterable<Entity> {

	public EntityPool(int type) {
		mSize = INITIAL_SIZE;
		mList = new Entity[mSize];
		mNextIndex = 0;
		mRecycledIDs = new HashSet<Integer>();
		
		// Keep track of the number of collision points among all entities.
		mNumCollisionPoints = 0;
		
		mBodies = new Quadtree(Quadtree.X, Entity.CollisionRadii[type], new Point2[1]);
	}
	
	public int add(Entity entity) {
		
		int id = Entity.NO_ENTITY;
		
		if (!mRecycledIDs.isEmpty()) {
			id = mRecycledIDs.iterator().next();
			mRecycledIDs.remove(id);
		}
		else {
			// Grow if necessary. This shouldn't happen much.
			if (mNextIndex >= mSize) {
				Entity[] oldList = mList;
				mList = new Entity[mSize * 2];
				for (int i = 0; i < mSize; i++) {
					mList[i] = oldList[i];
				}
				mSize *= 2;
			}
			
			id = mNextIndex;
			mNextIndex++;
		}
		
		mList[id] = entity;
		
		entity.id = id;
		entity.body.center.id = id;
		for (Point2 extraPoint : entity.mExtraPoints) {
			extraPoint.id = id;
		}
		
		mNumCollisionPoints += entity.mExtraPoints.length + 1;
		
		return id;
	}
	
	public Entity get(int id) {
		return mList[id];
	}
	
	public int size() {
		return mNextIndex - mRecycledIDs.size();
	}
	
	public boolean isEmpty() {
		return size() <= 0;
	}
	
	public void clear() {
		mNextIndex = 0;
		mRecycledIDs.clear();
		mBodies.reset(0, 0);
	}

	public Iterator<Entity> iterator() {
		return new EntityPoolIterator(this, mList, mNextIndex);
	}
	
	public Entity collide(Point2 center, float radius) {
		return collide(center.x, center.y, radius);
	}
	
	public Entity collide(float centerX, float centerY, float radius) {
		Entity entity = null;
		Point2 p = mBodies.collide(centerX, centerY, radius);
		if (p != null) {
			entity = get(p.id);
		}
		return entity;
	}
	
	public void invalidateCollisionSpaces() {
		mBodies.invalidate();
	}
	
	public void rebuildCollisionSpaces() {
		
		// Grow the body tree's points array if necessary.
		if (mNumCollisionPoints > mBodies.mPoints.length) {
			int numBodies = mBodies.mPoints.length;
			while (numBodies < mNumCollisionPoints) {
				numBodies *= 2;
			}
			mBodies.mPoints = new Point2[numBodies];
		}

		int i = 0;

		// Tweak all of the points in the quadtree, then reset it.
		for (Entity e : this) {
			mBodies.mPoints[i] = e.body.center;
			i++;
			for (Point2 extraPoint : e.mExtraPoints) {
				mBodies.mPoints[i] = extraPoint;
				i++;
			}
		}
		mBodies.reset(0, i);
	}
	
	public void remove(int id) {
		assert(id != Entity.NO_ENTITY);
		remove(mList[id]);
	}
	
	public void remove(Entity entity) {

		assert(entity.id != Entity.NO_ENTITY);
		assert(entity.id == entity.body.center.id);
		
		mList[entity.id] = null;
		mRecycledIDs.add(entity.id);
		mBodies.remove(entity.body.center);
		for (Point2 extraPoint : entity.mExtraPoints) {
			mBodies.remove(extraPoint);
		}
		
		mNumCollisionPoints -= entity.mExtraPoints.length + 1;
		
		entity.id = Entity.NO_ENTITY;
		entity.body.center.id = Entity.NO_ENTITY;
	}

	// Use a set to eliminate duplicates and ensure that an ID can't be recycled twice.
	//private List<Entity> mList;
	private Set<Integer> mRecycledIDs;
	
	private Entity[] mList;
	private int mNextIndex;
	private int mSize;
	private static final int INITIAL_SIZE = 1;
	
	private int mNumCollisionPoints;
	
	protected Quadtree mBodies;
	
	private static final long serialVersionUID = 9001749495484315418L;
}
