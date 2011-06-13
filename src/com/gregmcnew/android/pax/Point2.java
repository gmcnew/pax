package com.gregmcnew.android.pax;

public class Point2 implements Comparable<Point2> { 
	
	public Point2(float x, float y) {
		mX = x;
		mY = y;
	}
	
	public float distanceToSquared(Point2 other) {
		float dx = other.mX - mX;
		float dy = other.mY - mY;
		return (dx * dx) + (dy * dy);
	}
	
	public float distanceTo(Point2 other) {
		return (float) Math.sqrt(distanceToSquared(other));
	}
	
	public Point2 add(Point2 other) {
		return new Point2(mX + other.mX, mY + other.mY);
	}
	
	public Point2 add(float x, float y) {
		return new Point2(mX + x, mY + y);
	}
	
	public Point2 subtract(Point2 other) {
		return new Point2(mX - other.mX, mY - other.mY);
	}
	
	@Override
	public String toString() {
		return String.format("(%f,%f)", mX, mY);
	}
	
	public int compareTo(Point2 another) {
		int result = 0;
		
		if (mX < another.mX) {
			result = -1;
		}
		else if (mX > another.mX) {
			result = 1;
		}
		else if (mY < another.mY) {
			result = -1;
		}
		else if (mY > another.mY) {
			result = 1;
		}
		
		return result;
	}
	
	public final float mX;
	public final float mY;
}
