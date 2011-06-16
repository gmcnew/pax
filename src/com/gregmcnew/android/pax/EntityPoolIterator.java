package com.gregmcnew.android.pax;

import java.util.Iterator;
import java.util.List;

public class EntityPoolIterator implements Iterator<Entity> {

	public EntityPoolIterator(EntityPool hal, List<Entity> list) {
		mHal = hal;
		mList = list;
		i = 0;
	}
	
	@Override
	public boolean hasNext() {
		int t = i;
		int size = mList.size();
		
		// Skip null (recycled) entries.
		while (t < size && mList.get(t) == null) {
			t++;
		}
		
		return t < size;
	}

	@Override
	public Entity next() {
		int size = mList.size();
		
		// Skip null (recycled) entries.
		while (i < size && mList.get(i) == null) {
			i++;
		}
		
		return mList.get(i++);
	}

	@Override
	public void remove() {
		mHal.remove(i);
	}
	
	private EntityPool mHal;
	private List<Entity> mList;
	private int i;

}
