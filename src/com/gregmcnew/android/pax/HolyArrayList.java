package com.gregmcnew.android.pax;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/*
 * An ArrayList capable of reusing holes!
 */

public class HolyArrayList<E> implements Iterable<E> {

	public HolyArrayList() {
		mList = new ArrayList<E>();
		mRecycledIDs = new HashSet<Integer>();
	}
	
	public int add(E e) {
		int id;
		if (mRecycledIDs.isEmpty()) {
			id = mList.size();
			mList.add(e);
		}
		else {
			id = mRecycledIDs.iterator().next();
			mRecycledIDs.remove(id);
			mList.set(id, e);
		}
		return id;
	}
	
	public E get(int id) {
		return mList.get(id);
	}
	
	public int size() {
		return mList.size() - mRecycledIDs.size();
	}
	
	public void clear() {
		mList.clear();
	}

	public Iterator<E> iterator() {
		return new HolyArrayListIterator<E>(this, mList);
	}
	
	public void remove(int id) {
		mList.set(id, null);
		mRecycledIDs.add(id);
	}

	// Use a set to eliminate duplicates and ensure that an ID can't be recycled twice.
	private List<E> mList;
	private Set<Integer> mRecycledIDs;
	
	private static final long serialVersionUID = 9001749495484315418L;
}
