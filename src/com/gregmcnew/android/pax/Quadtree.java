package com.gregmcnew.android.pax;

import java.util.Stack;

public class Quadtree {
	
	// Static members and methods.

	public static final boolean X = false;
	public static final boolean Y = true;

	private static final int MAX_LEAF_SIZE = 5;
	
	private static final Stack<Quadtree> sRecycled = new Stack<Quadtree>();
	
	public static Quadtree create(boolean dimension, float entrySize, Point2[] points) {
		Quadtree quadtree = sRecycled.isEmpty() ? new Quadtree() : sRecycled.pop();
		return quadtree.reset(dimension, entrySize, points);
	}
	
	
	// Public methods
	
	public void clear() {
		reset(mPoints, 0, 0);
	}
	
	public void invalidate() {
		mIsValid = false;
	}
	
	public void print() {
		print(0);
	}
	
	public void rebuild(EntityPool entityPool, int numCollisionPoints) {
		
		// Grow our points array if necessary.
		if (numCollisionPoints > mPoints.length) {
			int numBodies = mPoints.length;
			while (numBodies < numCollisionPoints) {
				numBodies *= 2;
			}
			mPoints = new Point2[numBodies];
		}

		int i = 0;

		// Tweak all of our points, then reset.
		for (Entity e : entityPool) {
			mPoints[i] = e.body.center;
			i++;
			for (Point2 extraPoint : e.mExtraPoints) {
				mPoints[i] = extraPoint;
				i++;
			}
		}
		
		reset(mPoints, 0, i);
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
	
	//private static Stack<Quadtree> as = new Stack<Quadtree>();
	//private static Stack<Quadtree> bs = new Stack<Quadtree>();
	
	public static void collide(Quadtree treeA, Quadtree treeB, CollisionHandler ch) {
		
		float radii = treeA.mEntrySize + treeB.mEntrySize;

		boolean outOfRange =
				(treeB.mMaxX + radii < treeA.mMinX) ||
				(treeB.mMinX - radii > treeA.mMaxX) ||
				(treeB.mMaxY + radii < treeA.mMinY) ||
				(treeB.mMinY - radii > treeA.mMaxY);

		if (outOfRange) {
			return;
		}

		if (treeA.isLeaf) {
			for (int i = treeA.mMinIndex; i < treeA.mMaxIndex; i++) {
				Point2 a = treeA.mPoints[i];
				Point2 b = treeB.collide(a, treeA.mEntrySize);
				while (b != null) {
					int result = ch.collide(a, b);
					
					if (0 != (result & CollisionHandler.REMOVE_B)) {
						treeB.remove(b);
					}
					
					if (0 != (result & CollisionHandler.REMOVE_A)) {
						treeA.mPoints[i] = treeA.mPoints[treeA.mMaxIndex - 1];
						treeA.mMaxIndex--;
						i--;
						break;
					}
					b = treeB.collide(a, treeA.mEntrySize);
				}
			}
		}
		else {
			if (treeA.mDepth > treeB.mDepth) {
				collide(treeA.low, treeB, ch);
				collide(treeA.high, treeB, ch);
			}
			else {
				collide(treeA, treeB.low, ch);
				collide(treeA, treeB.high, ch);
			}
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
			float lowMinVal = (low.mDimension == X) ? low.mMinX : low.mMinY;
			float lowMaxVal = (low.mDimension == X) ? low.mMaxX : low.mMaxY;
			float highMinVal = (low.mDimension == X) ? high.mMinX : high.mMinY;
			float highMaxVal = (low.mDimension == X) ? high.mMaxX : high.mMaxY;
			
			if (q >= lowMinVal && q <= lowMaxVal) {
				removed = low.remove(point);
			}
			else if (q >= highMinVal && q <= highMaxVal) {
				removed = high.remove(point);
			}
		}
		
		return removed;
	}
	
	
	// Private methods
	
	private Quadtree() {
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
		sRecycled.push(this);
	}
	
	private Quadtree reset(boolean dimension, float entrySize, Point2[] points) {
		
		mEntrySize = entrySize;
		mDimension = dimension;
		
		return reset(points, 0, 0);
	}
	
	private void resetMinMaxValues() {
		boolean first = true;
		for (int i = mMinIndex; i < mMaxIndex; i++) {
			float q = (mDimension == X) ? mPoints[i].x : mPoints[i].y;
			if (first) {
				mMinX = mMaxX = mPoints[i].x;
				mMinY = mMaxY = mPoints[i].y;
				first = false;
			}
			else {
				mMinX = Math.min(mMinX, mPoints[i].x);
				mMaxX = Math.max(mMaxX, mPoints[i].x);
				mMinY = Math.min(mMinY, mPoints[i].y);
				mMaxY = Math.max(mMaxY, mPoints[i].y);
			}
		}
	}
	
	private Quadtree reset(Point2[] points, int minIndex, int maxIndex) {
		
		mPoints = points;
		
		mMinIndex = minIndex;
		mMaxIndex = maxIndex;
		
		isLeaf = (mMaxIndex - mMinIndex) <= MAX_LEAF_SIZE;
		mDepth = 0;
		
		resetMinMaxValues();
		
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

				mDepth = Math.max(low.mDepth, high.mDepth) + 1;
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
	
	// Return the closest point in this node that's within 'radius' of 'center'.
	// We pass radius as well as radiusSquared in order to reduce square-root
	// calculations.
	private Point2 collide(float centerX, float centerY, float radius, float radiusSquared) {
		Point2 closest = null;
		
		// If the point plus the radius lies outside of mMinVal and mMaxVail,
		// stop here.
		float q = (mDimension == X) ? centerX : centerY;
		float minVal = (mDimension == X) ? mMinX : mMinY;
		float maxVal = (mDimension == X) ? mMaxX : mMaxY;
		if (q + radius < minVal || q - radius > maxVal) {
			return closest;
		}
		
		if (isLeaf) {
			for (int i = mMinIndex; i < mMaxIndex; i++) {
				Point2 point = mPoints[i];
				
				// First, make sure our the point collides with the square that
				// contains the circle. If it doesn't, we can save ourselves
				// some multiplications.
				float dx = point.x - centerX;
				float dy = point.y - centerY;
				
				boolean inSquare = (-radius <= dx && dx <= radius) && (-radius <= dy && dy <= radius);
				if (inSquare) {
					float distanceSquared = (dx * dx) + (dy * dy);
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
			// Collide with the low node.
			closest = low.collide(centerX, centerY, radius, radiusSquared);
				
			if (closest != null) {
				// Limit our search even further, since
				// low.collide() already found something.
				radiusSquared = closest.distanceToSquared(centerX, centerY);
				radius = (float) Math.sqrt(radiusSquared);
			}
			
			Point2 rightClosest = high.collide(centerX, centerY, radius, radiusSquared);
			if (rightClosest != null) {
				closest = rightClosest;
			}
		}
		
		return closest;
	}
	
	private static String spaces = "                                          ";
	
	private void print(int depth) {
		PLog.v("Quadtree.print", String.format("%s%s goes from %s=[%f..%f] (indices %d..%d)",
				spaces.substring(0, depth * 2), (isLeaf ? "leaf" : "node"), (mDimension == X ? "x" : "y"),
				(mDimension == X ? mMinX : mMinY), (mDimension == X ? mMaxX : mMaxY), mMinIndex, mMaxIndex - 1));
		if (isLeaf) {
			for (int i = mMinIndex; i < mMaxIndex; i++) {
				PLog.v("Quadtree.print", String.format("%s- point %d: (%f,%f) with ID %d",
						spaces.substring(0, depth * 2), i, mPoints[i].x, mPoints[i].y, mPoints[i].id));
			}
		}
		else {
			depth++;
			low.print(depth);
			high.print(depth);
		}
	}


	// Accessors used by QuadtreePainter.

	public boolean getIsLeaf() {
		return isLeaf;
	}

	public boolean getDimension() {
		return mDimension;
	}

	public Quadtree getLow() {
		return low;
	}

	public Quadtree getHigh() {
		return high;
	}

	public float getMinX() {
		return mMinX;
	}
	public float getMaxX() {
		return mMaxX;
	}
	public float getMinY() {
		return mMinY;
	}
	public float getMaxY() {
		return mMaxY;
	}

	private Point2[] mPoints;
	
	private boolean mDimension;
	private boolean isLeaf;
	private int mMinIndex;
	// mMaxIndex shrinks when nodes are removed.
	private int mMaxIndex;
	
	// Not used by leaves
	private Quadtree low;
	private Quadtree high;

	protected int mDepth;
	
	// These aren't updated when entries are removed, but that's okay.
	// Removals are rare, so it'd probably be inefficient to recalculate these.
	private float mMinX, mMinY, mMaxX, mMaxY;
	
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
