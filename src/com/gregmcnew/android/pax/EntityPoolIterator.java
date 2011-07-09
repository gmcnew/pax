package com.gregmcnew.android.pax;

import java.util.Iterator;

public class EntityPoolIterator implements Iterator<Entity> {
	
	// Static members and methods
	
	// (If we ever need more than 10 iterators at once, we're doing something
	// wrong.)
	private static final EntityPoolIterator sIterators[] = new EntityPoolIterator[10];
	private static int sNextIterator = -1;
	
	// Entities are enumerated extremely frequently, so it's important that
	// creating an EntityPoolIterator is fast. Recycling helps.
	public static EntityPoolIterator create(EntityPool entityPool, Entity[] list, int maxIndex) {
		EntityPoolIterator iterator = sNextIterator < 0
			? new EntityPoolIterator()
			: sIterators[sNextIterator--];
		return iterator.initialize(entityPool, list, maxIndex);
	}
	
	
	// Object members and methods

	private EntityPoolIterator() {
	}
	
	private EntityPoolIterator initialize(EntityPool entityPool, Entity[] list, int maxIndex) {
		mEntityPool = entityPool;
		mList = list;
		mMaxIndex = maxIndex;
		i = 0;
		return this;
	}
	
	@Override
	public boolean hasNext() {

		// Skip null (recycled) entries, and advance i, since we don't want it
		// to be pointing at null entries.
		while (i < mMaxIndex && mList[i] == null) {
			i++;
		}
		
		boolean hasNext = (i < mMaxIndex);
		if (!hasNext) {
			// We assume that if an iterator has no more entries, it won't be
			// used any more, so it can be recycled.
			sIterators[++sNextIterator] = this;
		}
		
		return hasNext;
	}

	@Override
	public Entity next() {
		return mList[i++];
	}

	@Override
	public void remove() {
		mEntityPool.remove(i);
	}
	
	private EntityPool mEntityPool;
	private Entity[] mList;
	private int mMaxIndex;
	private int i;
}
