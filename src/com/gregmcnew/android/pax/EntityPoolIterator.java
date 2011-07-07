package com.gregmcnew.android.pax;

import java.util.Iterator;
import java.util.Stack;

public class EntityPoolIterator implements Iterator<Entity> {
	
	// Static members and methods
	
	private static final Stack<EntityPoolIterator> sIterators = new Stack<EntityPoolIterator>();
	
	public static EntityPoolIterator create(EntityPool entityPool, Entity[] list, int maxIndex) {
		EntityPoolIterator iterator = sIterators.isEmpty()
			? new EntityPoolIterator() 
			: sIterators.pop();
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
			// used any more, so it can be recycled. (Note: Stack.push() is just
			// a wrapper for Vector.addElement(). Calling Vector.addElement()
			// directly shaves about 4% off the runtime, according to
			// benchmarks.)
			sIterators.addElement(this);
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
