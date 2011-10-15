package com.gregmcnew.android.pax;

import java.util.Iterator;

public class EntityVectorIterator implements Iterator<Entity> {
	
	// Static members and methods
	
	// (If we ever need more than 10 iterators at once, we're doing something
	// wrong.)
	private static final EntityVectorIterator sIterators[] = new EntityVectorIterator[10];
	private static int sNextIterator = -1;
	
	// Entities are enumerated extremely frequently, so it's important that
	// creating an EntityPoolIterator is fast. Recycling helps.
	public static EntityVectorIterator create(EntityVector entityVector) {
		EntityVectorIterator iterator = sNextIterator < 0
			? new EntityVectorIterator()
			: sIterators[sNextIterator--];
		return iterator.initialize(entityVector);
	}
	
	
	// Object members and methods

	private EntityVectorIterator() {
	}
	
	private EntityVectorIterator initialize(EntityVector entityVector) {
		mVect = entityVector;
		i = 0;
		return this;
	}
	
	@Override
	public boolean hasNext() {

		// Skip null (recycled) entries, and advance i, since we don't want it
		// to be pointing at null entries.
		while (i < mVect.mSize && mVect.mData[i] == null) {
			i++;
		}
		
		boolean hasNext = (i < mVect.mSize);
		if (!hasNext) {
			// We assume that if an iterator has no more entries, it won't be
			// used any more, so it can be recycled.
			sIterators[++sNextIterator] = this;
		}
		
		return hasNext;
	}

	@Override
	public Entity next() {
		return mVect.mData[i++];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	private EntityVector mVect;
	private int i;
}
