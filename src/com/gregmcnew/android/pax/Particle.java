package com.gregmcnew.android.pax;

public class Particle {
	
	// Static members and methods.
	
	private static final Pool<Particle> sPool = new Pool<Particle>(Particle.class);
	
	public static Particle create() {
		return sPool.create();
	}
	
	
	// Object members and methods.
	
	protected Particle() {
	}
	
	public void recycle() {
		sPool.recycle(this);
	}
	
	public void reset(long Life, float Scale, float X, float Y, float VelX, float VelY) {
		life = Life;
		scale = Scale;
		x = X;
		y = Y;
		velX = VelX;
		velY = VelY;
	}

	public long life; // in milliseconds
	public float scale;
	public float x;
	public float y;
	public float velX;
	public float velY;
}