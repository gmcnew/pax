package com.gregmcnew.android.pax;

/** Represents a circle. Stores its center and radius. **/
public class CircleF {
	
	public CircleF(Point2 Center, float Radius) {
		center = Center;
		radius = Radius;
	}
	
	public Point2 center;
	public final float radius;
}
