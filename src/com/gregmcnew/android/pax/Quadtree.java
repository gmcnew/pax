package com.gregmcnew.android.pax;

import android.util.Log;

public class Quadtree {
	
	// Static members and methods.

	public static final boolean X = false;
	public static final boolean Y = true;
	
	private static final int MAX_SIZE = 5;
	
	private static final Pool<Quadtree> sPool = new Pool<Quadtree>(Quadtree.class);
	
	public static Quadtree create(boolean dimension, float entrySize, Point2[] points) {
		return sPool.create().reset(dimension, entrySize, points);
	}
	
	// Object members and methods.
	
	protected Quadtree() {
	}
	
	private void recycle() {
		if (low != null) {
			low.recycle();
			low = null;
		}
		if (high != null) {
			high.recycle();
			high = null;
		}
		sPool.recycle(this);
	}
	
	private Quadtree reset(boolean dimension, float entrySize, Point2[] points) {
		
		mEntrySize = entrySize;
		mDimension = dimension;
		
		return reset(points, 0, 0);
	}
	
	public Quadtree reset(Point2[] points, int minIndex, int maxIndex) {
		
		mPoints = points;
		
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
			
			// If partitioning fails (e.g., perhaps all points are equal in the
			// given dimension), just treat this node as a leaf.
			if (pivotIndex <= mMinIndex || pivotIndex >= mMaxIndex) {
				isLeaf = true;
			}
			else {
				if (low == null) {
					low = Quadtree.create(!mDimension, mEntrySize, mPoints);
				}
				
				if (high == null) {
					high = Quadtree.create(!mDimension, mEntrySize, mPoints);
				}
				low.reset(mPoints, mMinIndex, pivotIndex);
				high.reset(mPoints, pivotIndex, mMaxIndex);
			}
		}
		
		if (isLeaf) {
			if (low != null) {
				low.recycle();
				low = null;
			}
			if (high != null) {
				high.recycle();
				high = null;
			}
		}
		
		mIsValid = true;
		
		return this;
	}
	
	public Point2 collide(Point2 center, float radius) {
		assert(mIsValid);
		radius += mEntrySize;
		return collide(center.x, center.y, radius, radius * radius);
	}
	
	public Point2 collide(float centerX, float centerY, float radius) {
		assert(mIsValid);
		radius += mEntrySize;
		return collide(centerX, centerY, radius, radius * radius);
	}
	
	// Return the closest point in this node that's within 'radius' of 'center'.
	// We pass radius as well as radiusSquared in order to reduce square-root
	// calculations.
	private Point2 collide(float centerX, float centerY, float radius, float radiusSquared) {
		Point2 closest = null;
		
		if (isLeaf) {
			for (int i = mMinIndex; i < mMaxIndex; i++) {
				Point2 point = mPoints[i];
				
				// First, make sure our the point collides with the square that
				// contains the circle. If it doesn't, we can save ourselves
				// some multiplications.
				float absDx = point.x - centerX;
				float absDy = point.y - centerY;
				if (absDx < 0) {
					absDx = -absDx;
				}
				if (absDy < 0) {
					absDy = -absDy;
				}
				if (absDx <= radius && absDy <= radius) {
					float distanceSquared = (absDx * absDx) + (absDy + absDy);
					if (distanceSquared < radiusSquared) {
						
						// Shorten the search distance, since there may be more
						// points to check.
						
						// We'll leave 'radius' alone. We could recalculate it
						// with Math.sqrt(), but that would almost certainly be
						// slower than leaving it alone and allowing unnecessary
						// checks to occur for other points in this leaf.
						
						radiusSquared = distanceSquared;
						closest = point;
					}
				}
			}
		}
		else {
			float q = (low.mDimension == X) ? centerX : centerY;
			
			if (q + radius >= low.mMinVal && q - radius <= low.mMaxVal) {
				closest = low.collide(centerX, centerY, radius, radiusSquared);
			}
			if (q + radius >= high.mMinVal && q - radius <= high.mMaxVal) {
				
				if (closest != null) {
					// Limit our search even further, since
					// left.collide() already found something.
					radiusSquared = closest.distanceToSquared(centerX, centerY);
					radius = (float) Math.sqrt(radiusSquared);
				}
				
				Point2 rightClosest = high.collide(centerX, centerY, radius, radiusSquared);
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
		if (isLeaf) {
			for (int i = mMinIndex; i < mMaxIndex && !removed; i++) {
				if (mPoints[i].equals(point)) {
					// Replace this point with the one at the end of our range,
					// then remove the point at the end of our range.
					mMaxIndex--;
					mPoints[i] = mPoints[mMaxIndex];
					mPoints[mMaxIndex] = null;
					removed = true;
				}
			}
		}
		else {
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
	
	protected Point2[] mPoints;
	
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
	private float mEntrySize;
	
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
