package com.gregmcnew.android.pax;

import android.graphics.PointF;

/** Represents a circle. Stores its center and radius. **/
public class CircleF {
	
	public CircleF(PointF Center, float Radius) {
		center = Center;
		radius = Radius;
	}
	
	public PointF center;
	public float radius;
}
