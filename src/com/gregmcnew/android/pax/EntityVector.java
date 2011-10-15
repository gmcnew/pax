package com.gregmcnew.android.pax;

import java.util.Iterator;

// A FastVector exposes its data array to avoid the need for iterators (which
// require allocations).

public class EntityVector implements Iterable<Entity>{
	
	// Public members and methods
	
	public EntityVector() {
		mCapacity = 1;
		mSize = 0;
		mData = new Entity[mCapacity];
	}
	
	@Override
	public Iterator<Entity> iterator() {
		return EntityVectorIterator.create(this);
	}
	
	public int add(Entity entity) {
		// Grow if necessary.
		if (mSize >= mCapacity) {
			grow(mCapacity * 2);
		}
		
		mData[mSize] = entity;
		mSize++;
		
		return mSize - 1;
	}
	
	public void clear() {
		mSize = 0;
	}
	
	public Entity[] mData;
	public int mSize;
	
	
	// Private members and methods
	
	private void grow(int newCapacity) {
		mCapacity = newCapacity;
		
		Entity[] newData = new Entity[mCapacity];
		
		for (int i = 0; i < mSize; i++) {
			newData[i] = mData[i];
		}
		
		mData = newData;
	}
	
	private int mCapacity;
}
