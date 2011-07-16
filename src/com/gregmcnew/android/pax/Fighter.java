package com.gregmcnew.android.pax;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Fighter extends Ship {
	
	public static final int HEALTH = 40;
	public static final float DIAMETER = 7.5f;
	public static final float TURN_SPEED = 1.875f; // in radians per second
	public static final float[]ACCELERATIONLIMS = {120, 60};
	public static final float MAXSPEED = 150;
	public static final int[] TARGET_PRIORITIES = { Entity.BOMBER, Entity.FIGHTER, Entity.FRIGATE, Entity.FACTORY };
	public static final int SHOT_INTERVAL = 6;
	
	private static final long TRAIL_LENGTH_MS = 250;
	private static final long TRAIL_POINT_INTERVAL = 50;
	
	public static final float MIN_PREFERRED_TARGET_DISTANCE = 250;
	
	protected Fighter() {
		super(Entity.FIGHTER, TARGET_PRIORITIES, null, HEALTH, DIAMETER, DIAMETER, TURN_SPEED, ACCELERATIONLIMS, MAXSPEED);
		
		int numTrailPoints = (int) (TRAIL_LENGTH_MS / TRAIL_POINT_INTERVAL);
		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocateDirect(numTrailPoints * Short.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mTrailVertices = byteBuffer.asShortBuffer();
		
		byteBuffer = ByteBuffer.allocateDirect(numTrailPoints * Short.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mTempTrailVertices = byteBuffer.asShortBuffer();
		
		byteBuffer = ByteBuffer.allocateDirect(numTrailPoints * 4 * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mVertexColors = byteBuffer.asFloatBuffer();
		
		mTempTrailVertices.position(0);
		mTrailVertices.position(0);
		
		mOriginalBufferLimit = mTrailVertices.limit();
		
		reset(null);
	}
	
	@Override
	public void reset(Ship parent) {
		super.reset(parent);
		reloadTimeMs = 6000;
		shotTimeMs = 33;
		clipSize = 5;
		shotsLeft = clipSize;

		mTempTrailVertices.limit(0);
		mTrailVertices.limit(0);
		trailPointBudget = 0;
		
		// Run away by default. This avoids scenarios in which enemy ships are
		// hovering over our factory and newly-spawned fighters turn in circles
		// uselessly instead of moving far enough away to get a decent shot.
		mIsRunningAway = true;
	}
	
	long mSmokeBudgetMs = 0;
	private static final long SMOKE_INTERVAL_MS = 20;
	@Override
	public void emitParticles(Emitter[] emitters, long dt) {
		

		if (Pax.sJetStreams) {
			mSmokeBudgetMs += dt;
			if (mSmokeBudgetMs > SMOKE_INTERVAL_MS) {
				mSmokeBudgetMs -= SMOKE_INTERVAL_MS;
				emitters[Emitter.SMOKE].add(8f, body.center.x, body.center.y,
						(Pax.sRandom.nextFloat() - 0.5f) * 10,
						(Pax.sRandom.nextFloat() - 0.5f) * 10);
			}
		}
		/*
		
		trailPointBudget += dt;
		
		// TODO: Move most of this to a separate object so (1) the code is
		// cleaner and (2) the stream is still drawn after the fighter dies.
		if (!Pax.sJetStreams) {
			mTrailVertices.position(0);
			mTrailVertices.limit(0);
			mTempTrailVertices.position(0);
			mTempTrailVertices.limit(0);
		}
		else if (trailPointBudget >= TRAIL_POINT_INTERVAL) {
			trailPointBudget -= TRAIL_POINT_INTERVAL;
			
			mTempTrailVertices.position(0);
			mTrailVertices.position(0);
			mVertexColors.position(0);
			
			int newLimit = mTrailVertices.limit() + 4;
			if (newLimit > mOriginalBufferLimit) {
				newLimit = mOriginalBufferLimit;
			}
			mTempTrailVertices.limit(newLimit);
			
			short x = (short) body.center.x;
			short y = (short) body.center.y;
			float dx = (float) Math.sin(heading) * 5;
			float dy = (float) Math.cos(heading) * 5;
			if (x != 0 && y != 0) {
				mTempTrailVertices.put((short) (x - dx));
				mTempTrailVertices.put((short) (y - dy));
				mTempTrailVertices.put((short) (x + dx));
				mTempTrailVertices.put((short) (y + dy));
				for (int i = 0; i < 8; i++) {
					//mVertexColors.put(0.5f);
				}
			}
			
			int i = mTempTrailVertices.position();
			while (i < mTempTrailVertices.capacity() && mTrailVertices.hasRemaining()) {
				mTempTrailVertices.put(mTrailVertices.get());
				i++;
				for (int j = 0; j < 4; j++) {
					//mVertexColors.put(0.5f);
				}
			}
			mTempTrailVertices.limit(i);
			
			int numVertices = i / 2;
			mVertexColors.position(0);
			for (i = 0; i < numVertices * 2; i++) {
				mVertexColors.put(1f);
				mVertexColors.put(1f);
				mVertexColors.put(1f);
				float alpha = (float) (numVertices - i) / numVertices;
				mVertexColors.put(alpha);
			}
			
			mTempTrailVertices.position(0);
			mTrailVertices.position(0);
			
			ShortBuffer temp = mTrailVertices;
			mTrailVertices = mTempTrailVertices;
			mTempTrailVertices = temp;
		}
		*/
	}
	
	private long trailPointBudget;
	
	private int mOriginalBufferLimit;
	protected ShortBuffer mTrailVertices;
	protected ShortBuffer mTempTrailVertices;
	protected FloatBuffer mVertexColors;
	
	protected boolean mIsRunningAway;
	
}
