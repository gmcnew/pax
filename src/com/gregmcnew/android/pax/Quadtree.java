package com.gregmcnew.android.pax;

import android.util.Log;

public class Quadtree {

	public static final boolean X = false;
	public static final boolean Y = true;
	
	private static final int MAX_SIZE = 5;
	
	public Quadtree(boolean dimension, float entrySize, Point2[] points) {
		
		mPoints = points;
		
		mEntrySize = entrySize;
		
		mDimension = dimension;
		
		mMinIndex = 0;
		mMaxIndex = 0;
		
		reset(mMinIndex, mMaxIndex);
	}
	public void reset(int minIndex, int maxIndex) {
		
		mMinIndex = minIndex;
		mMaxIndex = maxIndex;
		
		isLeaf = (mMaxIndex - mMinIndex) <= MAX_SIZE;
		
		// Set mMin and mMax
		boolean first = true;
		for (int i = mMinIndex; i < mMaxIndex; i++) {
			float q = (mDimension == X) ? mPoints[i].x : mPoints[i].y;
			if (first) {
				mMinVal = q;
				mMaxVal = q;
				first = false;
			}
			else if (q < mMinVal) {
				mMinVal = q;
			}
			else if (q > mMaxVal) {
				mMaxVal = q;
			}
		}
		
		if (!isLeaf) {
			// If this node covers a range in the X dimension (for example), we
			// want to partition its elements in the Y dimension. After
			// partitioning, create 'low' and 'high' nodes for the low and high
			// ranges of the Y-values.
			int pivotIndex = partition(!mDimension, mPoints, mMinIndex, mMaxIndex);
			
			if (low == null) {
				low = new Quadtree(!mDimension, mEntrySize, mPoints);
			}
			
			if (high == null) {
				high = new Quadtree(!mDimension, mEntrySize, mPoints);
			}
			
			low.reset(mMinIndex, pivotIndex);
			high.reset(pivotIndex, mMaxIndex);
		}
		else {
			low = null;
			high = null;
		}
		
		mIsValid = true;
	}
	
	public Point2 collide(Point2 center, float radius) {
		assert(mIsValid);
		radius += mEntrySize;
		return collide(center, radius, radius * radius);
	}
	
	// Return the closest point in this node that's within 'radius' of 'center'.
	// We pass radius as well as radiusSquared in order to reduce square-root
	// calculations.
	private Point2 collide(Point2 center, float radius, float radiusSquared) {
		Point2 closest = null;
		
		if (isLeaf) {
			for (int i = mMinIndex; i < mMaxIndex; i++) {
				Point2 point = mPoints[i];
				float distanceSquared = point.distanceToSquared(center);
				if (distanceSquared < radiusSquared) {
					// Shorten the search distance, since there may be more
					// points to check. No need to recalculate 'radius', since
					// we won't use it.
					radiusSquared = distanceSquared;
					closest = point;
				}
			}
		}
		else {
			float q = (low.mDimension == X) ? center.x : center.y;
			
			if (q + radius >= low.mMinVal && q - radius <= low.mMaxVal) {
				closest = low.collide(center, radius, radiusSquared);
			}
			if (q + radius >= high.mMinVal && q - radius <= high.mMaxVal) {
				
				if (closest != null) {
					// Limit our search even further, since
					// left.collide() already found something.
					radiusSquared = closest.distanceToSquared(center);
					radius = (float) Math.sqrt(radiusSquared);
				}
				
				Point2 rightClosest = high.collide(center, radius, radiusSquared);
				if (rightClosest != null) {
					closest = rightClosest;
				}
			}
		}
		
		return closest;
	}
	
	public void invalidate() {
		mIsValid = false;
	}
	
	public void print() {
		print(0);
	}
	
	private static String spaces = "                                          ";
	
	private void print(int depth) {
		Log.v("Quadtree.print", String.format("%s%s goes from %s=[%f..%f] (indices %d..%d)",
				spaces.substring(0, depth * 2), (isLeaf ? "leaf" : "node"), (mDimension == X ? "x" : "y"),
				mMinVal, mMaxVal, mMinIndex, mMaxIndex - 1));
		if (isLeaf) {
			for (int i = mMinIndex; i < mMaxIndex; i++) {
				Log.v("Quadtree.print", String.format("%s- point %d: (%f,%f) with ID %d",
						spaces.substring(0, depth * 2), i, mPoints[i].x, mPoints[i].y, mPoints[i].id));
			}
		}
		else {
			depth++;
			low.print(depth);
			high.print(depth);
		}
	}
	
	// Returns true if the point was removed.
	public boolean remove(Point2 point) {
		assert(mIsValid);
		boolean removed = false;
		Log.v("Quadtree.remove", String.format("removing point (%f,%f) with ID %d", point.x, point.y, point.id));
		if (isLeaf) {
			Log.v("Quadtree.remove", String.format("removing from leaf %d(%f --> %f) with %d points", mDimension ? 0 : 1, mMinVal, mMaxVal, mMaxIndex - mMinIndex));
			for (int i = mMinIndex; i < mMaxIndex && !removed; i++) {
				if (mPoints[i].equals(point)) {
					// Replace this point with the one at the end of our range,
					// then remove the point at the end of our range.
					mMaxIndex--;
					mPoints[i] = mPoints[mMaxIndex];
					mPoints[mMaxIndex] = null;
					removed = true;
				}
				else if (mPoints[i].x == point.x && mPoints[i].y == point.y) {
					Log.i("Quadtree.remove", String.format("interesting! duplicate point (%f,%f), different ID (%d vs %d)", point.x, point.y, point.id, mPoints[i].id));
				}
				else if (mPoints[i].id == point.id) {
					Log.i("Quadtree.remove", String.format("interesting! different point (%f,%f) vs (%f,%f), duplicate ID (%d)", point.x, point.y, mPoints[i].x, mPoints[i].y, point.id));
				}
			}
		}
		else {
			Log.v("Quadtree.remove", String.format("removing from node with %d points", mMaxIndex - mMinIndex));
			float q = (low.mDimension == X) ? point.x : point.y;
			
			if (q >= low.mMinVal && q <= low.mMaxVal) {
				removed = low.remove(point);
			}
			else if (q >= high.mMinVal && q <= high.mMaxVal) {
				removed = high.remove(point);
			}
		}
		
		return removed;
	}
	
	protected final Point2[] mPoints;
	
	private boolean mDimension;
	private boolean isLeaf;
	private int mMinIndex;
	// mMaxIndex shrinks when nodes are removed.
	private int mMaxIndex;
	
	// Not used by leaves
	private Quadtree low;
	private Quadtree high;
	
	// These aren't updated when entries are removed, but that's okay.
	// Removals are rare, so it'd probably be inefficient to recalculate these.
	private float mMinVal;
	private float mMaxVal;
	
	// The tree stores points, but these points actually represent the centers
	// of circular collision bodies. This means we need to add the circle's
	// radius whenever collide() is called. (We're guaranteed that all entries
	// in a given quadtree are for circles of the same size.)
	private final float mEntrySize;
	
	private boolean mIsValid;
	
	private static int partition(boolean dimension, Point2[] points, int minIndex, int maxIndex) {
		
		// Pick a pivot value that bisects this range.
		float min = 0;
		float max = 0;
		boolean first = true;
		for (int i = minIndex; i < maxIndex; i++) {
			float q = (dimension == X) ? points[i].x : points[i].y;
			if (first) {
				min = q;
				max = q;
				first = false;
			}
			else if (q < min) {
				min = q;
			}
			else if (q > max) {
				max = q;
			}
		}
		float pivotValue = (min + max) / 2;
		
		int hole = minIndex;
		
		for (int i = minIndex; i < maxIndex; i++) {
			float q = (dimension == X) ? points[i].x : points[i].y;
			if (q < pivotValue) {
				// swap points[hole] and points[i]
				Point2 temp = points[hole];
				points[hole] = points[i];
				points[i] = temp;
				hole++;
			}
		}
		
		return hole;
	}
}
