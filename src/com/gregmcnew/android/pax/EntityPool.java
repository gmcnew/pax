package com.gregmcnew.android.pax;

import java.util.Iterator;
import java.util.Stack;

public class EntityPool implements Iterable<Entity> {

	public EntityPool(int type) {
		mCapacity = INITIAL_SIZE;
		mList = new Entity[mCapacity];
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
			entity = createNewEntity(entityType, parent);
			
			if (entity != null) {
				// Grow if necessary. This shouldn't happen much.
				if (mNextIndex >= mCapacity) {
					Entity[] oldList = mList;
					mList = new Entity[mCapacity * 2];
					for (int i = 0; i < mCapacity; i++) {
						mList[i] = oldList[i];
					}
					mCapacity *= 2;
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
		mBodies.clear();
	}

	public Iterator<Entity> iterator() {
		return EntityPoolIterator.create(this, mList, mNextIndex);
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
		mBodies.rebuild(this, mNumCollisionPoints);
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
	
	private Entity createNewEntity(int entityType, Ship parent) {
		Entity entity = null;

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
		
		return entity;
	}
	
	public int getCapacity() {
		return mCapacity;
	}
	
	private Stack<Entity> mRecycledEntities;
	
	private Entity[] mList;
	private int mNextIndex;
	private int mCapacity;
	private static final int INITIAL_SIZE = 1;
	
	private int mNumCollisionPoints;
	
	protected Quadtree mBodies;
}
