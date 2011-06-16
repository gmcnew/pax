package com.gregmcnew.android.pax;

import java.util.Iterator;
import java.util.List;

public class EntityPoolIterator implements Iterator<Entity> {

	public EntityPoolIterator(EntityPool hal, List<Entity> list) {
		mHal = hal;
		mList = list;
		mSize = list.size();
		i = 0;
	}
	
	@Override
	public boolean hasNext() {

		// Skip null (recycled) entries. It's okay to advance i, since we don't
		// want it pointing at null entries anyway.
		while (i < mSize && mList.get(i) == null) {
			i++;
		}
		
		return i < mSize;
	}

	@Override
	public Entity next() {
		// Skip null (recycled) entries.
		while (i < mSize && mList.get(i) == null) {
			i++;
		}
		
		return mList.get(i++);
	}

	@Override
	public void remove() {
		mHal.remove(i);
	}
	
	private final EntityPool mHal;
	private final List<Entity> mList;
	private final int mSize;
	private int i;

}
