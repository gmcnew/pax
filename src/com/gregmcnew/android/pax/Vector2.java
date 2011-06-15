package com.gregmcnew.android.pax;

public class Vector2 {
	
	public Vector2(Point2 a, Point2 b, int tag) {
		mA = a;
		mB = b;
		
		mX = mB.x - mA.x;
		mY = mB.y - mA.y;
		
		mTag = tag;
	}
	
	@Override
	public String toString() {
		return String.format("%s->%s", mA, mB);
	}
	
	public Float intersectionHitTime(Vector2 otherVector) {
		
		float crossProduct = crossZ(otherVector);

		if (crossProduct == 0) {
			return null;
		}
		float hitTime = -otherVector.crossZ(mA, otherVector.mA) / crossProduct;
		return hitTime;
	}
	
	public Vector2(Vector2 otherVector, float startHitTime, float endHitTime) {
		float dx = otherVector.mX;
		float dy = otherVector.mY;
		mA = otherVector.mA.add(dx * startHitTime, dy * startHitTime);
		mB = otherVector.mA.add(dx * endHitTime, dy * endHitTime);
		mX = mB.x - mA.x;
		mY = mB.y - mA.y;
		mTag = otherVector.mTag;
	}
	
	public float dot(Vector2 otherVector) {
		return (mX * otherVector.mX) + (mY * otherVector.mY);
	}
	
	public float crossZ(Vector2 otherVector) {
		return (mX * otherVector.mY) - (mY * otherVector.mX);
	}
	
	public float crossZ(Point2 otherA, Point2 otherB) {
		float otherX = otherB.x - otherA.x;
		float otherY = otherB.y - otherA.y;
		return (mX * otherY) - (mY * otherX);
	}
	
	public Vector2 perpendicular() {
		return new Vector2(mA, mA.add(-mY, mX), mTag);
	}
	
	public Vector2 intersectCircle(Point2 center, float radius) {
		
		Vector2 solution = null;

		Vector2 d = this;
		Vector2 f = new Vector2(center, mA, mTag);
		
		float a = d.dot(d);
		float b = 2 * f.dot(d);
		float c = f.dot(f) - (radius * radius);
		float discriminantSquared = (b * b) - (4 * a * c);
		
		if (discriminantSquared <= 0) {
			// The line doesn't intersect (or is a tangent). 
		}
		else {
			float discriminant = (float) Math.sqrt(discriminantSquared);
			float t1 = (-b - discriminant) / (2 * a);
			float t2 = (-b + discriminant) / (2 * a);
			
			// Clamp hit times to the closest points on the segment. If both hit
			// times are on the same side of the segment (i.e., t1 < t2 < 0 or
			// 1 < t1 < t2), t1 < t2 will no longer be true after clamping.
			if (t1 < 0) {
				t1 = 0;
			}
			if (t2 > 1) {
				t2 = 1;
			}

			if (t1 < t2) {
				solution = new Vector2(getHitPoint(t1), getHitPoint(t2), mTag);
			}
		}
		
		return solution;
	}
	
	public Point2 getHitPoint(float hitTime) {
		return mA.add(mX * hitTime, mY * hitTime);
	}
	
	public final int mTag;
	public final Point2 mA;
	public final Point2 mB;
	public final float mX;
	public final float mY;
}
