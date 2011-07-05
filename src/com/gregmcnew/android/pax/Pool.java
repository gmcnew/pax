package com.gregmcnew.android.pax;

import java.util.Stack;

public class Pool<E> {
	
	public Pool(Class<E> c) {
		mClass = c;
		mRecycled = new Stack<E>();
	}
	
	public void recycle(E e) {
		mRecycled.push(e);
	}
	
	public E create() {
		E object = null;
		if (!mRecycled.isEmpty()) {
			object = mRecycled.pop();
		}
		try {
			object = mClass.newInstance();
		} catch (IllegalAccessException e) {
		} catch (InstantiationException e) {
		}
		return object;
	}
	
	private final Class<E> mClass;
	private final Stack<E> mRecycled;
}
