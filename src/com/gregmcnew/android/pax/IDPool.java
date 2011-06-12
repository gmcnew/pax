package com.gregmcnew.android.pax;

import java.util.HashSet;
import java.util.Set;

public class IDPool {
	
	public IDPool() {
		recycledIDs = new HashSet<Integer>();
		nextID = MIN_ID;
	}
	
	public int get() {
		int id;
		if (recycledIDs.isEmpty()) {
			id = nextID++;
		}
		else {
			id = recycledIDs.iterator().next();
			recycledIDs.remove(id);
		}
		return id;
	}
	
	public void recycle(int id) {
		if (MIN_ID <= id && id < nextID) {
			recycledIDs.add(id);
		}
	}
	
	// Use a set to eliminate duplicates and ensure that an ID can't be recycled twice.
	private Set<Integer> recycledIDs;
	private static int MIN_ID = 0;
	private int nextID;

}
