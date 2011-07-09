package com.gregmcnew.android.pax;

public class Point2 implements Comparable<Point2> {
	
	public Point2() {
		this(0.0f, 0.0f);
	}
	
	public Point2(Point2 other) {
		x = other.x;
		y = other.y;
		id = other.id;
	}
	
	public Point2(float X, float Y) {
		x = X;
		y = Y;
		id = Entity.NO_ENTITY;
	}
	
	public float distanceToSquared(Point2 other) {
		float dx = other.x - x;
		float dy = other.y - y;
		return (dx * dx) + (dy * dy);
	}
	
	public float distanceToSquared(float otherX, float otherY) {
		float dx = otherX - x;
		float dy = otherY - y;
		return (dx * dx) + (dy * dy);
	}
	
	public boolean equals(Point2 other) {
		return (x == other.x && y == other.y && id == other.id); 
	}
	
	public float distanceTo(Point2 other) {
		return (float) Math.sqrt(distanceToSquared(other));
	}
	
	public void set(float X, float Y) {
		x = X;
		y = Y;
	}
	
	public void set(Point2 other) {
		x = other.x;
		y = other.y;
	}
	
	public void add(float dx, float dy) {
		x += dx;
		y += dy;
	}
	
	public void add(Point2 other) {
		add(other.x, other.y);
	}
	
	public void subtract(Point2 other) {
		add(-other.x, -other.y);
	}
	
	@Override
	public String toString() {
		return String.format("(%f,%f)", x, y);
	}
	
	public int compareTo(Point2 another) {
		int result = 0;
		
		if (x < another.x) {
			result = -1;
		}
		else if (x > another.x) {
			result = 1;
		}
		else if (y < another.y) {
			result = -1;
		}
		else if (y > another.y) {
			result = 1;
		}
		
		return result;
	}
	
	public float x;
	public float y;
	public int id;
}
