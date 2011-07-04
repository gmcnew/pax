package com.gregmcnew.android.pax;

import java.util.Iterator;
import java.util.Stack;

public class EntityPoolIterator implements Iterator<Entity> {
	
	// Static members and methods
	
	public static final Stack<EntityPoolIterator> sIterators = new Stack<EntityPoolIterator>();
	
	protected static EntityPoolIterator create() {
		if (sIterators.isEmpty()) {
			return new EntityPoolIterator();
		}
		return sIterators.pop();
	}
	
	
	// Object members and methods

	private EntityPoolIterator() {
	}
	
	protected EntityPoolIterator initialize(EntityPool hal, Entity[] list, int maxIndex) {
		mHal = hal;
		mList = list;
		mMaxIndex = maxIndex;
		i = 0;
		return this;
	}
	
	@Override
	public boolean hasNext() {

		// Skip null (recycled) entries. It's okay to advance i, since we don't
		// want it pointing at null entries anyway.
		while (i < mMaxIndex && mList[i] == null) {
			i++;
		}
		
		boolean hasNext = (i < mMaxIndex);
		if (!hasNext) {
			// We assume that if an iterator has no more entries, it won't be
			// used any more in its current scope, so it can be recycled.
			sIterators.add(this);
		}
		
		return hasNext;
	}

	@Override
	public Entity next() {
		// Skip null (recycled) entries.
		while (i < mMaxIndex && mList[i] == null) {
			i++;
		}
		
		return mList[i++];
	}

	@Override
	public void remove() {
		mHal.remove(i);
	}
	
	private EntityPool mHal;
	private Entity[] mList;
	private int mMaxIndex;
	private int i;
}
