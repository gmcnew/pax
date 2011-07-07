package com.gregmcnew.android.pax;

import java.util.Iterator;
import java.util.Stack;

public class EntityPool implements Iterable<Entity> {

	public EntityPool(int type) {
		mSize = INITIAL_SIZE;
		mList = new Entity[mSize];
		mNextIndex = 0;
		mRecycledEntities = new Stack<Entity>();
		
		// Keep track of the number of collision points among all entities.
		mNumCollisionPoints = 0;
		
		mBodies = Quadtree.create(Quadtree.X, Entity.CollisionRadii[type], new Point2[1]);
	}
	
	public Entity add(int entityType, Ship parent) {
		
		Entity entity = null;
		if (!mRecycledEntities.isEmpty()) {
			entity = mRecycledEntities.pop();
			entity.reset(parent);
		}
		else {
			switch (entityType) {
				case Entity.FIGHTER:
					entity = new Fighter();
					break;
				case Entity.BOMBER:
					entity = new Bomber();
					break;
				case Entity.FRIGATE:
					entity = new Frigate();
					break;
				case Entity.FACTORY:
					entity = new Factory();
					break;
				case Entity.LASER:
					entity = new Laser(parent);
					break;
				case Entity.BOMB:
					entity = new Bomb(parent);
					break;
				case Entity.MISSILE:
					entity = new Missile(parent);
					break;
			}
			
			if (entity != null) {
				// Grow if necessary. This shouldn't happen much.
				if (mNextIndex >= mSize) {
					Entity[] oldList = mList;
					mList = new Entity[mSize * 2];
					for (int i = 0; i < mSize; i++) {
						mList[i] = oldList[i];
					}
					mSize *= 2;
				}
				
				entity.id = mNextIndex;
				mNextIndex++;
			}
		}
		
		if (entity != null) {
			
			mList[entity.id] = entity;
			
			entity.body.center.id = entity.id;
			for (Point2 extraPoint : entity.mExtraPoints) {
				extraPoint.id = entity.id;
			}
			
			mNumCollisionPoints += entity.mExtraPoints.length + 1;
		}
		
		return entity;
	}
	
	public Entity get(int id) {
		return mList[id];
	}
	
	public int size() {
		return mNextIndex - mRecycledEntities.size();
	}
	
	public boolean isEmpty() {
		return size() <= 0;
	}
	
	public void clear() {
		for (Entity e : this) {
			remove(e);
		}
		mBodies.reset(mBodies.mPoints, 0, 0);
	}

	public Iterator<Entity> iterator() {
		return EntityPoolIterator.create().initialize(this, mList, mNextIndex);
	}
	
	public Entity collide(Point2 center, float radius) {
		return collide(center.x, center.y, radius);
	}
	
	public Entity collide(float centerX, float centerY, float radius) {
		Point2 p = mBodies.collide(centerX, centerY, radius);
		return (p == null) ? null : get(p.id);
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
		mBodies.reset(mBodies.mPoints, 0, i);
	}
	
	protected void remove(int id) {
		assert(id != Entity.NO_ENTITY);
		remove(mList[id]);
	}
	
	public void remove(Entity entity) {

		assert(entity.id != Entity.NO_ENTITY);
		assert(entity.id == entity.body.center.id);
		
		mList[entity.id] = null;
		mRecycledEntities.push(entity);
		mBodies.remove(entity.body.center);
		for (Point2 extraPoint : entity.mExtraPoints) {
			mBodies.remove(extraPoint);
		}
		
		mNumCollisionPoints -= entity.mExtraPoints.length + 1;
	}
	
	private Stack<Entity> mRecycledEntities;
	
	private Entity[] mList;
	private int mNextIndex;
	private int mSize;
	private static final int INITIAL_SIZE = 1;
	
	private int mNumCollisionPoints;
	
	protected Quadtree mBodies;
}
