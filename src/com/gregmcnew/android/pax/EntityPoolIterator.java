package com.gregmcnew.android.pax;

import java.util.Iterator;

public class EntityPoolIterator implements Iterator<Entity> {

	public EntityPoolIterator(EntityPool hal, Entity[] list, int maxIndex) {
		mHal = hal;
		mList = list;
		mMaxIndex = maxIndex;
		i = 0;
	}
	
	@Override
	public boolean hasNext() {

		// Skip null (recycled) entries. It's okay to advance i, since we don't
		// want it pointing at null entries anyway.
		while (i < mMaxIndex && mList[i] == null) {
			i++;
		}
		
		return i < mMaxIndex;
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
	
	private final EntityPool mHal;
	private final Entity[] mList;
	private final int mMaxIndex;
	private int i;

}
