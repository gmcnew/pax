package com.gregmcnew.android.pax;

import java.util.Stack;

public class Particle {
	
	// Static members and methods.
	
	private static final Stack<Particle> sRecycled = new Stack<Particle>();
	
	public static Particle create() {
		return sRecycled.isEmpty() ? new Particle() : sRecycled.pop();
	}
	
	
	// Object members and methods.
	
	private Particle() {
	}
	
	public void recycle() {
		sRecycled.push(this);
	}
	
	public void reset(long Life, float Scale, float X, float Y, float VelX, float VelY, float Accel) {
		life = Life;
		scale = Scale;
		x = X;
		y = Y;
		velX = VelX;
		velY = VelY;
		accel = Accel;
	}

	public long life; // in milliseconds
	public float scale;
	public float x;
	public float y;
	public float velX;
	public float velY;
	public float accel;
}