package com.gregmcnew.android.pax;

import android.graphics.PointF;

public class CircleF {
	
	public CircleF(PointF Center, float Radius) {
		center = Center;
		radius = Radius;
	}
	
	public float distanceToSquared(CircleF circ2) {
	    float dx = center.x - circ2.center.x;
	    float dy = center.y - circ2.center.y;
	    return dx * dx + dy * dy;
	}
	
	/*
	public float distanceTo(CircleF circ2) {
		return (float) Math.sqrt(distanceToSquared(circ2));
	}
	*/
	
	public final PointF center;
	public final float radius;
}
