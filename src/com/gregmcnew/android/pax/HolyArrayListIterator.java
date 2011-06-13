package com.gregmcnew.android.pax;

import java.util.Iterator;
import java.util.List;

public class HolyArrayListIterator<E> implements Iterator<E> {

	public HolyArrayListIterator(HolyArrayList<E> hal, List<E> list) {
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
	public E next() {
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
	
	private HolyArrayList<E> mHal;
	private List<E> mList;
	private int i;

}
